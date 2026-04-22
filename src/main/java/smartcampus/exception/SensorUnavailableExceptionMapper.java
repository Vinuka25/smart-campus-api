package smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;
import smartcampus.model.ErrorResponse;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    private static final Logger LOGGER = Logger.getLogger(SensorUnavailableExceptionMapper.class.getName());

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        LOGGER.warning("Sensor unavailable: " + exception.getMessage());
        ErrorResponse body = new ErrorResponse(403, "FORBIDDEN", exception.getMessage());
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(body).build();
    }
}
