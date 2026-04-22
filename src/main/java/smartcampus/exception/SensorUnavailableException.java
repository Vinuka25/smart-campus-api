package smartcampus.exception;

import java.util.logging.Logger;

public class SensorUnavailableException extends RuntimeException {
    private static final Logger LOGGER = Logger.getLogger(SensorUnavailableException.class.getName());

    public SensorUnavailableException(String message) {
        super(message);
        LOGGER.warning(message);
    }
}
