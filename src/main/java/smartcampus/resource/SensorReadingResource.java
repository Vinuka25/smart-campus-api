package smartcampus.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import smartcampus.exception.SensorUnavailableException;
import smartcampus.model.Sensor;
import smartcampus.model.SensorReading;
import smartcampus.repository.CampusRepository;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    private static final Logger LOGGER = Logger.getLogger(SensorReadingResource.class.getName());
    private final CampusRepository repository = CampusRepository.getInstance();
    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        LOGGER.info("Fetching readings for sensor " + sensorId);
        return repository.getReadings(sensorId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = repository.getSensor(sensorId);
        if (sensor.getStatus() != null
                && sensor.getStatus().toUpperCase(Locale.ROOT).equals("MAINTENANCE")) {
            throw new SensorUnavailableException(
                    "Sensor " + sensorId + " is in MAINTENANCE and cannot accept readings."
            );
        }

        SensorReading saved = repository.addReading(sensorId, reading);
        LOGGER.info("Added reading " + saved.getId() + " under sensor " + sensorId);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }
}
