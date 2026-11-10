package com.outfitai.common;

import org.springframework.http.HttpStatus;

/** Throw this from services to produce a clean JSON error with the given HTTP status. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
