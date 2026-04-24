package smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import smartcampus.model.ErrorResponse;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            int status = webEx.getResponse().getStatus();
            LOGGER.log(Level.WARNING, "Handled web exception with status " + status, exception);
            ErrorResponse body = new ErrorResponse(
                    status,
                    "HTTP_" + status,
                    exception.getMessage() == null ? "Request could not be processed." : exception.getMessage()
            );
            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(body)
                    .build();
        }

        LOGGER.log(Level.SEVERE, "Unhandled server error", exception);
        ErrorResponse body = new ErrorResponse(
                500,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact support."
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
