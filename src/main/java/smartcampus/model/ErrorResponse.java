package smartcampus.model;

import java.util.logging.Logger;

public class ErrorResponse {
    private static final Logger LOGGER = Logger.getLogger(ErrorResponse.class.getName());
    private int status;
    private String error;
    private String message;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        LOGGER.fine("ErrorResponse created with status=" + status);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
