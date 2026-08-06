package com.eyesofpriestess.vault.exception;

import jakarta.ws.rs.core.Response;

/** VaultException — Domain exception for Vault Sanctum. */
public class VaultException extends RuntimeException {

    public final Response.Status status;
    public final String errorCode;
    public final String field;

    private VaultException(Response.Status status, String errorCode, String message, String field) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.field = field;
    }

    public static VaultException badRequest(String code, String message) {
        return new VaultException(Response.Status.BAD_REQUEST, code, message, null);
    }

    public static VaultException badRequest(String code, String message, String field) {
        return new VaultException(Response.Status.BAD_REQUEST, code, message, field);
    }

    public static VaultException unauthorized(String code, String message) {
        return new VaultException(Response.Status.UNAUTHORIZED, code, message, null);
    }

    public static VaultException forbidden(String code, String message) {
        return new VaultException(Response.Status.FORBIDDEN, code, message, null);
    }

    public static VaultException notFound(String code, String message) {
        return new VaultException(Response.Status.NOT_FOUND, code, message, null);
    }

    public static VaultException conflict(String code, String message, String field) {
        return new VaultException(Response.Status.CONFLICT, code, message, field);
    }

    public static VaultException insufficientFunds(String message) {
        return new VaultException(Response.Status.BAD_REQUEST, "INSUFFICIENT_TREASURY", message, "amount");
    }

    public static VaultException internal(String message) {
        return new VaultException(Response.Status.INTERNAL_SERVER_ERROR, "VAULT_ERROR", message, null);
    }
}
