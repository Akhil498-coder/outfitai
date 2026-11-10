package com.outfitai.auth;

import java.util.Optional;

/** Seam for Google verification so tests can fake it (we can't call Google from tests). */
public interface GoogleTokenVerifier {

    /** Returns the identity if the token is genuine, unexpired and issued for our client id. */
    Optional<GoogleIdentity> verify(String idToken);
}
