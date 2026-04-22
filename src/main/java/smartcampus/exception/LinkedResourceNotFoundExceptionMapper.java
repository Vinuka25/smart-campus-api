package smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;
import smartcampus.model.ErrorResponse;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    private static final Logger LOGGER = Logger.getLogger(LinkedResourceNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        LOGGER.warning("Linked resource validation failed: " + exception.getMessage());
        ErrorResponse body = new ErrorResponse(422, "UNPROCESSABLE_ENTITY", exception.getMessage());
        return Response.status(422).type(MediaType.APPLICATION_JSON).entity(body).build();
    }
}
