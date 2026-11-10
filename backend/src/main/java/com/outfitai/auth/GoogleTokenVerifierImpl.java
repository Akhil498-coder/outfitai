package com.outfitai.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Checks Google's signature on the ID token (keys are fetched from Google and cached) and that the token
 * was issued for OUR client id, so a token minted for some other app can't be replayed against us.
 */
@Component
public class GoogleTokenVerifierImpl implements GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifierImpl.class);

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierImpl(@Value("${app.google.client-id}") String clientId)
            throws GeneralSecurityException, IOException {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(clientId))
                .build();
    }

    @Override
    public Optional<GoogleIdentity> verify(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                return Optional.empty();
            }
            GoogleIdToken.Payload p = token.getPayload();
            return Optional.of(new GoogleIdentity(p.getSubject(), p.getEmail(),
                    Boolean.TRUE.equals(p.getEmailVerified()), (String) p.get("name")));
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            log.warn("Google token verification failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
