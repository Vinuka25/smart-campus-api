package smartcampus.model;

import java.util.logging.Logger;

public class SensorReading {
    private static final Logger LOGGER = Logger.getLogger(SensorReading.class.getName());
    private String id;
    private long timestamp;
    private double value;

    public SensorReading() {
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
        LOGGER.fine("SensorReading model created for id=" + id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
