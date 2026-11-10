package com.outfitai.auth;

/** The facts we trust from a verified Google ID token. */
public record GoogleIdentity(String subject, String email, boolean emailVerified, String name) {
}
