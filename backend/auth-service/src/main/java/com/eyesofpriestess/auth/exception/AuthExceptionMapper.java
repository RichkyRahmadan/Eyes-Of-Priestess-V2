package com.eyesofpriestess.auth.exception;

import com.eyesofpriestess.auth.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * AuthExceptionMapper — Global JAX-RS exception mapper for Seal Sanctum.
 * Converts domain exceptions and validation errors into the standard ApiResponse format.
 */
@Provider
public class AuthExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(AuthExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {

        if (exception instanceof AuthException ex) {
            return Response
                .status(ex.status)
                .entity(ApiResponse.fail(ex.errorCode, ex.getMessage(), ex.field))
                .build();
        }

        if (exception instanceof ConstraintViolationException cvEx) {
            String message = cvEx.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse("Validation failed");
            String field = cvEx.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath().toString())
                .orElse(null);
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.fail("VALIDATION_ERROR", message, field))
                .build();
        }

        if (exception instanceof IllegalArgumentException iaEx) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.fail("INVALID_ARGUMENT", iaEx.getMessage(), null))
                .build();
        }

        // Unexpected errors
        LOG.errorf(exception, "Unexpected error in Seal Sanctum: %s", exception.getMessage());
        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(ApiResponse.fail("INTERNAL_ERROR",
                "An unexpected disturbance in the Sanctum occurred. Please try again."))
            .build();
    }
}
