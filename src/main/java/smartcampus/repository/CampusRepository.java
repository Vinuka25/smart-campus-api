package smartcampus.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import smartcampus.model.Room;
import smartcampus.model.Sensor;
import smartcampus.model.SensorReading;

public final class CampusRepository {
    private static final Logger LOGGER = Logger.getLogger(CampusRepository.class.getName());
    private static final CampusRepository INSTANCE = new CampusRepository();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private CampusRepository() {
    }

    public static CampusRepository getInstance() {
        return INSTANCE;
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Room createRoom(Room room) {
        LOGGER.info("Creating room " + room.getId());
        rooms.put(room.getId(), room);
        return room;
    }

    public Room deleteRoom(String roomId) {
        LOGGER.info("Deleting room " + roomId);
        return rooms.remove(roomId);
    }

    public List<Sensor> getAllSensors() {
        return new ArrayList<>(sensors.values());
    }

    public Sensor getSensor(String sensorId) {
        return sensors.get(sensorId);
    }

    public Sensor createSensor(Sensor sensor) {
        LOGGER.info("Creating sensor " + sensor.getId());
        sensors.put(sensor.getId(), sensor);
        sensorReadings.putIfAbsent(sensor.getId(), Collections.synchronizedList(new ArrayList<>()));

        Room room = rooms.get(sensor.getRoomId());
        if (room != null && !room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }
        return sensor;
    }

    public List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, Collections.emptyList());
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        sensorReadings.computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>())).add(reading);
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
        LOGGER.info("Added reading " + reading.getId() + " for sensor " + sensorId);
        return reading;
    }
}
