# Smart Campus API

RESTful API for campus room and sensor management, implemented for `5COSC022W Client-Server Architectures` using JAX-RS only.

## 1. Overview

This project implements a Smart Campus backend with:
- Room management (`/rooms`)
- Sensor registration and filtering (`/sensors`)
- Nested sensor reading history (`/sensors/{id}/readings`)
- Custom exception mapping and leak-proof global error handling
- Request/response logging via JAX-RS filters

The API is designed to match coursework constraints:
- JAX-RS (Jersey) only
- In-memory data structures only (no SQL/NoSQL database)
- Deployed on Apache Tomcat

## 2. Technology Stack

- Java 17
- Maven WAR
- Jersey 2.41 (JAX-RS with `javax.ws.rs`)
- Servlet API 4.0.1 (`javax.servlet`)
- Apache Tomcat 9
- JSON via Jackson provider (`jersey-media-json-jackson`)

## 3. Correct Project Folder Structure

```text
smart_campus_API/
├── pom.xml
├── README.md
├── nb-configuration.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── smartcampus/
│       │       ├── SmartCampusApplication.java
│       │       ├── RootRedirectServlet.java
│       │       ├── model/
│       │       │   ├── Room.java
│       │       │   ├── Sensor.java
│       │       │   ├── SensorReading.java
│       │       │   └── ErrorResponse.java
│       │       ├── repository/
│       │       │   └── CampusRepository.java
│       │       ├── resource/
│       │       │   ├── DiscoveryResource.java
│       │       │   ├── RoomResource.java
│       │       │   ├── SensorResource.java
│       │       │   └── SensorReadingResource.java
│       │       ├── exception/
│       │       │   ├── RoomNotEmptyException.java
│       │       │   ├── LinkedResourceNotFoundException.java
│       │       │   ├── SensorUnavailableException.java
│       │       │   ├── RoomNotEmptyExceptionMapper.java
│       │       │   ├── LinkedResourceNotFoundExceptionMapper.java
│       │       │   ├── SensorUnavailableExceptionMapper.java
│       │       │   └── GlobalExceptionMapper.java
│       │       └── filter/
│       │           └── ApiLoggingFilter.java
│       └── webapp/
│           ├── META-INF/
│           │   └── context.xml
│           └── WEB-INF/
│               └── web.xml
└── target/
```

## 4. Build and Run Instructions

### 4.1 Prerequisites

- JDK 17 installed
- Apache Tomcat 9 installed
- NetBeans IDE (recommended)
- Maven (optional if using NetBeans bundled Maven)

### 4.2 Run in NetBeans (Recommended)

1. Open NetBeans.
2. `File -> Open Project` and select `smart_campus_API`.
3. Verify Java platform is JDK 17:
   - Right click project -> `Properties -> Libraries -> Java Platform`.
4. Add Tomcat 9 server:
   - `Tools -> Servers -> Add Server -> Apache Tomcat`.
5. Set project Run server to Tomcat 9:
   - Right click project -> `Properties -> Run`.
6. Clean build:
   - Right click project -> `Clean and Build`.
7. Deploy:
   - Right click project -> `Run`.

### 4.3 URLs

- App root (auto-redirects):  
  `http://localhost:8080/smart-campus-api/`
- Discovery endpoint:  
  `http://localhost:8080/smart-campus-api/api/v1`

### 4.4 Terminal Build (Optional)

```bash
mvn clean package
```

WAR generated at:
`target/smart-campus-api.war`

## 5. API Design Summary

### Base API path
- `/api/v1`

### Endpoints

- Discovery:
  - `GET /api/v1`
- Rooms:
  - `GET /api/v1/rooms`
  - `POST /api/v1/rooms`
  - `GET /api/v1/rooms/{roomId}`
  - `DELETE /api/v1/rooms/{roomId}`
- Sensors:
  - `GET /api/v1/sensors`
  - `GET /api/v1/sensors?type=CO2`
  - `POST /api/v1/sensors`
- Sensor Readings (nested sub-resource):
  - `GET /api/v1/sensors/{sensorId}/readings`
  - `POST /api/v1/sensors/{sensorId}/readings`

## 6. Business Rules and Error Mapping

- Deleting room with linked sensors -> `409 Conflict`
- Creating sensor with unknown `roomId` -> `422 Unprocessable Entity`
- Posting reading to `MAINTENANCE` sensor -> `403 Forbidden`
- Unexpected runtime failure -> `500 Internal Server Error` (generic body, no stack trace leak)

## 7. Logging and Observability

`ApiLoggingFilter` implements:
- `ContainerRequestFilter`:
  - logs HTTP method + URI for incoming request
- `ContainerResponseFilter`:
  - logs final HTTP status for outgoing response

This centralizes logging as a cross-cutting concern.

## 8. Sample curl Commands (Required)

Set:
`BASE="http://localhost:8080/smart-campus-api/api/v1"`

### 1) Discovery
```bash
curl -i "$BASE"
```

### 2) Create room
```bash
curl -i -X POST "$BASE/rooms" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":120}"
```

### 3) Create second room
```bash
curl -i -X POST "$BASE/rooms" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LAB-101\",\"name\":\"Computer Lab\",\"capacity\":40}"
```

### 4) Create valid sensor
```bash
curl -i -X POST "$BASE/sensors" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":415.5,\"roomId\":\"LIB-301\"}"
```

### 5) Filter sensors by type
```bash
curl -i "$BASE/sensors?type=CO2"
```

### 6) Add reading
```bash
curl -i -X POST "$BASE/sensors/CO2-001/readings" \
  -H "Content-Type: application/json" \
  -d "{\"value\":420.2}"
```

### 7) Get reading history
```bash
curl -i "$BASE/sensors/CO2-001/readings"
```

### 8) Trigger 409 (delete occupied room)
```bash
curl -i -X DELETE "$BASE/rooms/LIB-301"
```

## 9. Postman Collection

Use the provided full collection JSON with embedded tests:
- discovery
- room CRUD scenarios
- sensor validation and filtering
- nested readings
- 409, 422, 403, 500 checks
- media-type mismatch 415 check

## 10. Report Answers (Coursework Questions)

### Part 1.1 - JAX-RS Resource Lifecycle
By default, JAX-RS resources are request-scoped: a new instance is typically created per request. Shared mutable state should therefore live in explicit shared repositories and must be thread-safe. In this implementation, shared in-memory data is stored in thread-safe structures (`ConcurrentHashMap`, synchronized lists), preventing race conditions and lost updates under concurrent requests.

### Part 1.2 - Why Hypermedia/HATEOAS
Hypermedia makes APIs self-descriptive at runtime. Clients discover valid next actions from responses rather than hardcoded endpoint assumptions. This improves evolvability: server-side URI changes have lower client impact when navigation is link-driven.

### Part 2.1 - IDs vs Full Objects in Room Lists
IDs-only responses reduce payload size and bandwidth usage but force clients to make extra calls for details. Full-object responses increase payload size but reduce client round trips and simplify client logic. The best choice depends on collection size and client needs.

### Part 2.2 - DELETE Idempotency
DELETE is idempotent in state terms. Repeating the same delete request does not produce additional state changes after the first terminal state is reached (deleted or blocked by constraint). This implementation keeps final state stable across retries.

### Part 3.1 - @Consumes JSON Mismatch
If a client sends a media type not accepted by `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS returns `415 Unsupported Media Type` because no compatible body reader is selected for that method.

### Part 3.2 - Why @QueryParam for Filtering
Filtering is naturally represented as optional query criteria on a collection (`/sensors?type=CO2`). Path-based alternatives imply rigid hierarchical resources and scale poorly when combining multiple filters.

### Part 4.1 - Sub-Resource Locator Benefits
Sub-resource locators improve separation of concerns. Sensor core operations and sensor-reading history logic remain in distinct classes, reducing controller complexity, improving maintainability, and mirroring domain hierarchy.

### Part 5.2 - Why 422 is Better than 404 Here
`404` applies to a missing target URI resource. Here, the URI exists, but the payload contains an invalid internal reference (`roomId`). Therefore `422 Unprocessable Entity` is semantically more precise.

### Part 5.4 - Stack Trace Exposure Risks
Raw traces can leak class names, package structure, file paths, framework versions, and internal control flow, enabling targeted attacks. Generic 500 responses reduce information disclosure while full details remain server-side in logs.

### Part 5.5 - Why Filters for Logging
Filters centralize logging as a cross-cutting concern, ensuring consistent behavior across endpoints and avoiding repetitive logging code in every resource method.

## 11. Video Demonstration Checklist

Ensure the demo shows:
- Discovery endpoint
- Room create/list/get
- Room delete success (204) and blocked delete (409)
- Sensor create with invalid room (422)
- Sensor filtering with changed query parameter
- Reading POST/GET nested sub-resource
- Parent sensor `currentValue` updated after reading
- Maintenance reading blocked (403)
- Global 500 generic response (no stack trace leak)
- Server log output for requests and responses

## 12. Important Compliance Notes

- Uses JAX-RS only (no Spring Boot)
- Uses in-memory data structures only (no DB)
- Compatible with Tomcat 9 (`javax` stack)
- Hosted as Maven project suitable for public GitHub submission
