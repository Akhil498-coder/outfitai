package com.outfitai.common;

/** Consistent JSON shape for every error the API returns. */
public record ApiError(int status, String error, String message) {
}
