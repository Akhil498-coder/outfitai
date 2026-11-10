package com.outfitai.user;

/** What the API shows about a user. Deliberately has no password hash. */
public record UserResponse(Long id, String email, String displayName, AuthProvider authProvider,
                           Integer age, Gender gender) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getDisplayName(), u.getAuthProvider(),
                u.getAge(), u.getGender());
    }
}
