package smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.logging.Logger;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(SmartCampusApplication.class.getName());

    public SmartCampusApplication() {
        LOGGER.info("SmartCampusApplication initialized");
    }
}
