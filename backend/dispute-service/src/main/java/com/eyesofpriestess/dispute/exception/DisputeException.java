package com.eyesofpriestess.dispute.exception;

import jakarta.ws.rs.core.Response;

public class DisputeException extends RuntimeException {

    public final Response.Status status;
    public final String errorCode;
    public final String field;

    private DisputeException(Response.Status status, String errorCode, String message, String field) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.field = field;
    }

    public static DisputeException badRequest(String code, String message) {
        return new DisputeException(Response.Status.BAD_REQUEST, code, message, null);
    }

    public static DisputeException forbidden(String code, String message) {
        return new DisputeException(Response.Status.FORBIDDEN, code, message, null);
    }

    public static DisputeException notFound(String code, String message) {
        return new DisputeException(Response.Status.NOT_FOUND, code, message, null);
    }

    public static DisputeException conflict(String code, String message, String field) {
        return new DisputeException(Response.Status.CONFLICT, code, message, field);
    }
}
