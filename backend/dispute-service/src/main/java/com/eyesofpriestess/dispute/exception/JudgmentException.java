package com.eyesofpriestess.dispute.exception;

import jakarta.ws.rs.core.Response;

public class JudgmentException extends RuntimeException {

    public final Response.Status status;
    public final String errorCode;
    public final String field;

    private JudgmentException(Response.Status status, String errorCode, String message, String field) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.field = field;
    }

    public static JudgmentException badRequest(String code, String message) {
        return new JudgmentException(Response.Status.BAD_REQUEST, code, message, null);
    }

    public static JudgmentException forbidden(String code, String message) {
        return new JudgmentException(Response.Status.FORBIDDEN, code, message, null);
    }

    public static JudgmentException notFound(String code, String message) {
        return new JudgmentException(Response.Status.NOT_FOUND, code, message, null);
    }

    public static JudgmentException conflict(String code, String message, String field) {
        return new JudgmentException(Response.Status.CONFLICT, code, message, field);
    }
}
