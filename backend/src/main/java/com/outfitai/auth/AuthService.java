package com.outfitai.auth;

import com.outfitai.common.ApiException;
import com.outfitai.user.AuthProvider;
import com.outfitai.user.User;
import com.outfitai.user.UserRepository;
import com.outfitai.user.UserResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String BAD_CREDENTIALS = "Invalid email or password";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleVerifier;
    /** Compared against when the email is unknown, so "no such user" takes as long as "wrong password". */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService,
                       GoogleTokenVerifier googleVerifier) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleVerifier = googleVerifier;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    public AuthResponse register(RegisterRequest req) {
        // BCrypt only uses the first 72 bytes; multi-byte characters can push us over the limit.
        if (req.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "password: must be at most 72 bytes");
        }
        String email = normalize(req.email());
        User existing = users.findByEmail(email).orElse(null);
        if (existing != null) {
            throw emailTaken(existing);
        }
        try {
            User saved = users.saveAndFlush(
                    User.local(email, passwordEncoder.encode(req.password()), req.displayName().trim()));
            return respond(saved);
        } catch (DataIntegrityViolationException e) { // lost a race with a parallel signup
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
    }

    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmail(normalize(req.email())).orElse(null);
        String hash = (user != null && user.getAuthProvider() == AuthProvider.LOCAL)
                ? user.getPasswordHash() : dummyHash;
        boolean matches = passwordEncoder.matches(req.password(), hash);
        if (user == null || user.getAuthProvider() != AuthProvider.LOCAL || !matches) {
            // One generic message for every failure so attackers can't discover which emails exist.
            throw new ApiException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }
        return respond(user);
    }

    /** Sign in (or first-time sign up) with a Google ID token obtained by the frontend. */
    public AuthResponse googleLogin(GoogleLoginRequest req) {
        GoogleIdentity identity = googleVerifier.verify(req.idToken())
                .filter(GoogleIdentity::emailVerified)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Google token"));

        // Returning Google user: matched by Google's stable id, not by email.
        User byGoogleId = users.findByGoogleSub(identity.subject()).orElse(null);
        if (byGoogleId != null) {
            return respond(byGoogleId);
        }
        String email = normalize(identity.email());
        if (users.existsByEmail(email)) { // accounts stay separate, so never merge into a password account
            throw new ApiException(HttpStatus.CONFLICT,
                    "This email is registered with a password. Log in with email and password.");
        }
        String name = (identity.name() == null || identity.name().isBlank())
                ? email.substring(0, email.indexOf('@')) : identity.name().trim();
        try {
            return respond(users.saveAndFlush(User.google(email, identity.subject(), name)));
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
    }

    AuthResponse respond(User user) {
        return new AuthResponse(jwtService.generate(user.getId(), user.getEmail()), UserResponse.from(user));
    }

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static ApiException emailTaken(User existing) {
        String message = existing.getAuthProvider() == AuthProvider.GOOGLE
                ? "This email is registered with Google sign-in. Use \"Sign in with Google\"."
                : "Email is already registered";
        return new ApiException(HttpStatus.CONFLICT, message);
    }
}
