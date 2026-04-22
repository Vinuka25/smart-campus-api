package smartcampus.exception;

import java.util.logging.Logger;

public class RoomNotEmptyException extends RuntimeException {
    private static final Logger LOGGER = Logger.getLogger(RoomNotEmptyException.class.getName());

    public RoomNotEmptyException(String message) {
        super(message);
        LOGGER.warning(message);
    }
}
