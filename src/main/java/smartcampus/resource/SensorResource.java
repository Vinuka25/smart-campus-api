package smartcampus.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import smartcampus.exception.LinkedResourceNotFoundException;
import smartcampus.model.Sensor;
import smartcampus.repository.CampusRepository;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {
    private static final Logger LOGGER = Logger.getLogger(SensorResource.class.getName());
    private final CampusRepository repository = CampusRepository.getInstance();

    @GET
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = repository.getAllSensors();
        if (type == null || type.isBlank()) {
            LOGGER.info("Fetching all sensors");
            return sensors;
        }

        String normalizedType = type.toLowerCase(Locale.ROOT);
        LOGGER.info("Filtering sensors by type: " + normalizedType);
        return sensors.stream()
                .filter(s -> s.getType() != null && s.getType().toLowerCase(Locale.ROOT).equals(normalizedType))
                .collect(Collectors.toList());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("sensor id is required").build();
        }
        if (sensor.getRoomId() == null || repository.getRoom(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException("Referenced roomId does not exist: " + sensor.getRoomId());
        }

        repository.createSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        LOGGER.info("Sensor created: " + sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource readingSubResource(@PathParam("sensorId") String sensorId) {
        if (repository.getSensor(sensorId) == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        LOGGER.info("Sub-resource routing to readings for sensor: " + sensorId);
        return new SensorReadingResource(sensorId);
    }
}
