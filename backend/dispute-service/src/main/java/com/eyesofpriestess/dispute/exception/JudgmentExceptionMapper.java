package com.eyesofpriestess.dispute.exception;

import com.eyesofpriestess.dispute.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class JudgmentExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(JudgmentExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof JudgmentException ex) {
            return Response.status(ex.status)
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
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.fail("VALIDATION_ERROR", message, field))
                    .build();
        }

        LOG.errorf(exception, "[JUDGMENT] Unexpected error: %s", exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.fail("INTERNAL_ERROR", "The Judgment Sanctum experienced an unforeseen disturbance."))
                .build();
    }
}
