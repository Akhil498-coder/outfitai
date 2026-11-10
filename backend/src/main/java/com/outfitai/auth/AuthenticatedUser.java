package com.outfitai.auth;

/** The "who is calling" object placed in Spring Security's context after the JWT is verified. */
public record AuthenticatedUser(Long id) {
}
