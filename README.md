# Smart Campus API (JAX-RS, `javax`, Tomcat 9)

RESTful API for managing campus rooms, sensors, and sensor readings using JAX-RS (Jersey), in-memory collections, and Apache Tomcat.

## Stack

- Java 17
- JAX-RS (`javax.ws.rs`)
- Jersey 2.41
- Maven WAR project
- Apache Tomcat 9 (Servlet 4 / `javax.servlet`)
- No database (in-memory `ConcurrentHashMap` / lists)

## Project Structure

- Base package: `smartcampus`
- API base path: `/api/v1`
- Main resources:
  - `/api/v1` (discovery)
  - `/api/v1/rooms`
  - `/api/v1/sensors`
  - `/api/v1/sensors/{sensorId}/readings`

## Prerequisites

- JDK 17 installed and selected in NetBeans
- Apache Maven installed (optional if using NetBeans Maven integration)
- Apache Tomcat 9 registered in NetBeans

## Run in NetBeans + Tomcat 9 (Recommended)

1. Open NetBeans.
2. `File -> Open Project` and select `smart_campus_API`.
3. Ensure project JDK is Java 17:
   - Right click project -> `Properties -> Libraries -> Java Platform`.
4. Add/configure Tomcat 9 in NetBeans:
   - `Tools -> Servers -> Add Server -> Apache Tomcat`.
5. Set Tomcat 9 as deployment target:
   - Right click project -> `Properties -> Run`.
   - Choose Server: `Tomcat 9`.
6. Clean and build:
   - Right click project -> `Clean and Build`.
7. Run/deploy:
   - Right click project -> `Run`.

If deployment succeeds, base URL will be similar to:

`http://localhost:8080/smart-campus-api/api/v1`

## Build from Terminal

From project root:

```bash
mvn clean package
```

WAR output:

`target/smart-campus-api.war`

Deploy this WAR to Tomcat 9 (`webapps`) if running manually.

## API Endpoints

### Discovery
- `GET /api/v1`

### Rooms
- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{roomId}`
- `DELETE /api/v1/rooms/{roomId}`

### Sensors
- `GET /api/v1/sensors`
- `GET /api/v1/sensors?type=CO2`
- `POST /api/v1/sensors`

### Sensor Readings (Sub-resource)
- `GET /api/v1/sensors/{sensorId}/readings`
- `POST /api/v1/sensors/{sensorId}/readings`

## Business Rules & Error Handling

- Room cannot be deleted if it still has sensors -> `409 Conflict`
- Sensor creation with non-existent `roomId` -> `422 Unprocessable Entity`
- Reading creation when sensor status is `MAINTENANCE` -> `403 Forbidden`
- Unhandled runtime errors -> `500 Internal Server Error` with generic JSON body (no stack trace leakage)

## Logging

- Request/response logging via JAX-RS filter:
  - Logs HTTP method + URI for incoming requests
  - Logs HTTP status for outgoing responses
- Additional logger usage added in repository/resources/mappers/models/exceptions

## Sample `curl` Commands

Assume:

`BASE="http://localhost:8080/smart-campus-api/api/v1"`

1) Discovery endpoint

```bash
curl -i "$BASE"
```

2) Create room

```bash
curl -i -X POST "$BASE/rooms" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":120}"
```

3) List rooms

```bash
curl -i "$BASE/rooms"
```

4) Create sensor (valid room link)

```bash
curl -i -X POST "$BASE/sensors" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":415.5,\"roomId\":\"LIB-301\"}"
```

5) Filter sensors by type

```bash
curl -i "$BASE/sensors?type=CO2"
```

6) Add reading for sensor

```bash
curl -i -X POST "$BASE/sensors/CO2-001/readings" \
  -H "Content-Type: application/json" \
  -d "{\"value\":420.2}"
```

7) Get sensor readings

```bash
curl -i "$BASE/sensors/CO2-001/readings"
```

8) Attempt to delete room with active sensor (expected `409`)

```bash
curl -i -X DELETE "$BASE/rooms/LIB-301"
```

## Notes on `javax` Migration

This project is intentionally configured for the `javax` namespace:

- JAX-RS imports are `javax.ws.rs.*`
- Servlet API dependency is `javax.servlet-api:4.0.1`
- Jersey is 2.x (2.41)
- `web.xml` uses Java EE 4.0 schema compatible with Tomcat 9

For this reason, use **Tomcat 9** (not Tomcat 10+) unless you migrate back to `jakarta.*`.
