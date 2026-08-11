package com.eyesofpriestess.room.exception;

import jakarta.ws.rs.core.Response;

public class RoomException extends RuntimeException {

    public final Response.Status status;
    public final String errorCode;
    public final String field;

    private RoomException(Response.Status status, String errorCode, String message, String field) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.field = field;
    }

    public static RoomException badRequest(String code, String message) {
        return new RoomException(Response.Status.BAD_REQUEST, code, message, null);
    }

    public static RoomException badRequest(String code, String message, String field) {
        return new RoomException(Response.Status.BAD_REQUEST, code, message, field);
    }

    public static RoomException unauthorized(String code, String message) {
        return new RoomException(Response.Status.UNAUTHORIZED, code, message, null);
    }

    public static RoomException forbidden(String code, String message) {
        return new RoomException(Response.Status.FORBIDDEN, code, message, null);
    }

    public static RoomException notFound(String code, String message) {
        return new RoomException(Response.Status.NOT_FOUND, code, message, null);
    }

    public static RoomException conflict(String code, String message, String field) {
        return new RoomException(Response.Status.CONFLICT, code, message, field);
    }
}
