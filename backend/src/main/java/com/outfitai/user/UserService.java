package com.outfitai.user;

import com.outfitai.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return UserResponse.from(find(userId));
    }

    /** Entity is loaded inside the transaction, so changing it is saved automatically at commit. */
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = find(userId);
        user.setDisplayName(req.displayName().trim());
        user.setAge(req.age());
        user.setGender(req.gender());
        return UserResponse.from(user);
    }

    private User find(Long userId) {
        // Valid token but the user no longer exists (e.g. deleted): treat as not authenticated.
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }
}
