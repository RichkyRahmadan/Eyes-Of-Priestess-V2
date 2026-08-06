package com.eyesofpriestess.seal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ApiResponse — Universal API response wrapper for all Seal Sanctum endpoints.
 * Format: {"success": true/false, "data": {...}} or {"success": false, "error": {...}}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    public boolean success;
    public T data;
    public ErrorBody error;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.error = new ErrorBody(code, message, null);
        return r;
    }

    public static <T> ApiResponse<T> fail(String code, String message, String field) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.error = new ErrorBody(code, message, field);
        return r;
    }

    // ─── Inner class for error body ────────────────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorBody {
        public String code;
        public String message;
        public String field;

        public ErrorBody(String code, String message, String field) {
            this.code = code;
            this.message = message;
            this.field = field;
        }
    }
}
