package com.eyesofpriestess.covenant.exception;

import jakarta.ws.rs.core.Response;

public class CovenantException extends RuntimeException {

    public final Response.Status status;
    public final String errorCode;
    public final String field;

    private CovenantException(Response.Status status, String errorCode, String message, String field) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.field = field;
    }

    public static CovenantException badRequest(String code, String message) {
        return new CovenantException(Response.Status.BAD_REQUEST, code, message, null);
    }

    public static CovenantException badRequest(String code, String message, String field) {
        return new CovenantException(Response.Status.BAD_REQUEST, code, message, field);
    }

    public static CovenantException unauthorized(String code, String message) {
        return new CovenantException(Response.Status.UNAUTHORIZED, code, message, null);
    }

    public static CovenantException forbidden(String code, String message) {
        return new CovenantException(Response.Status.FORBIDDEN, code, message, null);
    }

    public static CovenantException notFound(String code, String message) {
        return new CovenantException(Response.Status.NOT_FOUND, code, message, null);
    }

    public static CovenantException conflict(String code, String message, String field) {
        return new CovenantException(Response.Status.CONFLICT, code, message, field);
    }
}
