package com.eyesofpriestess.auth.exception;

import jakarta.ws.rs.core.Response;

/**
 * AuthException — Domain exception for Seal Sanctum business errors.
 * Carries an HTTP status code, error code, message, and optional field.
 */
public class AuthException extends RuntimeException {

    public final Response.Status status;
    public final String errorCode;
    public final String field;

    private AuthException(Response.Status status, String errorCode,
                          String message, String field) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.field = field;
    }

    // ─── Factory Methods ──────────────────────────────────────────────────────

    public static AuthException badRequest(String code, String message) {
        return new AuthException(Response.Status.BAD_REQUEST, code, message, null);
    }

    public static AuthException badRequest(String code, String message, String field) {
        return new AuthException(Response.Status.BAD_REQUEST, code, message, field);
    }

    public static AuthException unauthorized(String code, String message) {
        return new AuthException(Response.Status.UNAUTHORIZED, code, message, null);
    }

    public static AuthException forbidden(String code, String message) {
        return new AuthException(Response.Status.FORBIDDEN, code, message, null);
    }

    public static AuthException notFound(String code, String message) {
        return new AuthException(Response.Status.NOT_FOUND, code, message, null);
    }

    public static AuthException conflict(String code, String message, String field) {
        return new AuthException(Response.Status.CONFLICT, code, message, field);
    }

    public static AuthException tooManyRequests(String code, String message) {
        return new AuthException(Response.Status.TOO_MANY_REQUESTS, code, message, null);
    }

    public static AuthException internal(String message) {
        return new AuthException(Response.Status.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR", message, null);
    }
}
