package com.eyesofpriestess.wallet.exception;

import jakarta.ws.rs.core.Response;

/** WalletException — Domain exception for Vault Sanctum. */
public class WalletException extends RuntimeException {

    public final Response.Status status;
    public final String errorCode;
    public final String field;

    private WalletException(Response.Status status, String errorCode, String message, String field) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.field = field;
    }

    public static WalletException badRequest(String code, String message) {
        return new WalletException(Response.Status.BAD_REQUEST, code, message, null);
    }

    public static WalletException badRequest(String code, String message, String field) {
        return new WalletException(Response.Status.BAD_REQUEST, code, message, field);
    }

    public static WalletException unauthorized(String code, String message) {
        return new WalletException(Response.Status.UNAUTHORIZED, code, message, null);
    }

    public static WalletException forbidden(String code, String message) {
        return new WalletException(Response.Status.FORBIDDEN, code, message, null);
    }

    public static WalletException notFound(String code, String message) {
        return new WalletException(Response.Status.NOT_FOUND, code, message, null);
    }

    public static WalletException conflict(String code, String message, String field) {
        return new WalletException(Response.Status.CONFLICT, code, message, field);
    }

    public static WalletException insufficientFunds(String message) {
        return new WalletException(Response.Status.BAD_REQUEST, "INSUFFICIENT_TREASURY", message, "amount");
    }

    public static WalletException internal(String message) {
        return new WalletException(Response.Status.INTERNAL_SERVER_ERROR, "VAULT_ERROR", message, null);
    }
}
