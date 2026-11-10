package com.outfitai.auth;

import com.outfitai.user.UserResponse;

public record AuthResponse(String token, UserResponse user) {
}
