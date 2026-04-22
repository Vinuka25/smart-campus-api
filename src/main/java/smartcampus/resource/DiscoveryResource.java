package smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    private static final Logger LOGGER = Logger.getLogger(DiscoveryResource.class.getName());

    @GET
    public Map<String, Object> discover() {
        LOGGER.info("Serving discovery endpoint");
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Smart Campus API");
        payload.put("version", "v1");
        payload.put("contact", "campus-it@westminster.ac.uk");
        payload.put("resources", resources);
        return payload;
    }
}
