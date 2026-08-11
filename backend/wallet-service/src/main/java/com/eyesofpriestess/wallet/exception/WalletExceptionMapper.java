package com.eyesofpriestess.wallet.exception;

import com.eyesofpriestess.wallet.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class WalletExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(WalletExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WalletException ex) {
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

        LOG.errorf(exception, "Unexpected error in Vault Sanctum: %s", exception.getMessage());
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.fail("INTERNAL_ERROR", "An unexpected financial disturbance occurred in the Vault."))
                .build();
    }
}
