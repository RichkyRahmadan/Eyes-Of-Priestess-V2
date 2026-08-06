package com.eyesofpriestess.seal.exception;

import jakarta.ws.rs.core.Response;

/**
 * SealException — Domain exception for Seal Sanctum business errors.
 * Carries an HTTP status code, error code, message, and optional field.
 */
public class SealException extends RuntimeException {

    public final Response.Status status;
    public final String errorCode;
    public final String field;

    private SealException(Response.Status status, String errorCode,
                          String message, String field) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.field = field;
    }

    // ─── Factory Methods ──────────────────────────────────────────────────────

    public static SealException badRequest(String code, String message) {
        return new SealException(Response.Status.BAD_REQUEST, code, message, null);
    }

    public static SealException badRequest(String code, String message, String field) {
        return new SealException(Response.Status.BAD_REQUEST, code, message, field);
    }

    public static SealException unauthorized(String code, String message) {
        return new SealException(Response.Status.UNAUTHORIZED, code, message, null);
    }

    public static SealException forbidden(String code, String message) {
        return new SealException(Response.Status.FORBIDDEN, code, message, null);
    }

    public static SealException notFound(String code, String message) {
        return new SealException(Response.Status.NOT_FOUND, code, message, null);
    }

    public static SealException conflict(String code, String message, String field) {
        return new SealException(Response.Status.CONFLICT, code, message, field);
    }

    public static SealException tooManyRequests(String code, String message) {
        return new SealException(Response.Status.TOO_MANY_REQUESTS, code, message, null);
    }

    public static SealException internal(String message) {
        return new SealException(Response.Status.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR", message, null);
    }
}
