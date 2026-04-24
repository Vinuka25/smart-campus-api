# Smart Campus API

## 1. Overview

This project is a Java-based RESTful web service developed for the **Client-Server Architectures** coursework. It simulates a "Smart Campus" backend, providing endpoints to manage campus rooms, register IoT sensors (e.g., CO2, temperature), and log real-time telemetry data.

Built with **Java 17, JAX-RS (Jersey), and Apache Tomcat 9**, the API adheres to strict RESTful principles. In accordance with coursework constraints, it does not use an external database; instead, it utilizes thread-safe, in-memory data structures (`ConcurrentHashMap`) to securely handle concurrent client requests.

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

## 3. Project Structure

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
- NetBeans IDE
- Maven (optional if using NetBeans bundled Maven)

### 4.2 Run in NetBeans

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

## 8. Sample curl Commands

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

## 10. Report Answers

##Part 1: Service Architecture & Setup
###1. Project & Application Configuration

Question ;

In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

Answer ;

In JAX-RS Resource classes, by default, it is considered that it is “Request-scope,” which means that a fresh instance is created each time an HTTP request comes in, not treating the class as a Singleton. Multiple requests mean that multiple instances will run concurrently in separate threads, and the in-memory data structures would be extremely prone to issues related to race condition and corruption of data. In order to synchronize this process properly, we need to make sure that the repository works as a Singleton and uses thread-safe structures like ConcurrentHashMap for primary entity storage and Collections.synchronizedList() for nested arrays.

###2. The ”Discovery” Endpoint

Question ;

Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

Answer ;

HATEOAS (Hypermedia as the Engine of Application State) stands out as a defining characteristic of RESTful architecture owing to the way it turns the API into self-documenting system. Rather than depending on out-of-date documentation that is separate from the actual responses (and thus requiring the hard-coding of URIs), HATEOAS adds navigational links to the response data. This aspect is particularly useful for client developers since it enables them to dynamically identify possible actions and state changes in their application during runtime.

##Part 2: Room Management
###1. Room Resource Implementation

Question ;

When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing

Answer ;

Passing back only IDs helps to keep the size of the payload small, which results in lower network traffic usage. Nevertheless, if the interface requires room details, then this is very detrimental to client processing, because it leads to an N+1 query problem; that is, the client needs to make a new HTTP request for each ID to retrieve additional information, which causes higher latency. On the other hand, sending complete objects uses up more bandwidth at first, but it greatly cuts down on the number of requests made.

###2. Room Deletion & Safety Logic

Question ;

Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Answer ;

Yes, the DELETE method is completely idempotent. With idempotency, it means that sending the same request multiple times should yield the same result as if only one request was sent to the server. After executing the DELETE /rooms/LIB-301 for the first time, the server will delete the room from the data structure and then returns a successful status message. In case there is a repetition of sending the same DELETE /rooms/LIB-301 command, the server will check its repository and confirm that the room does not exist anymore (is null) and hence return a 204 Not Content without raising a 500 error or modifying any other data.

##Part 3: Sensor Operations & Linking

###1. Sensor Resource & Integrity

Question ;

We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

Answer ;

With the annotation of @Consumes(MediaType.APPLICATION_JSON) for the method, we make sure that the JAX-RS framework strictly adheres to the format of the payload being sent. In case a client fails to consider this and makes use of a Content-Type of either text/plain or application/xml, then technically speaking, JAX-RS would intercept the request before reaching the endpoint itself. As there is no corresponding MessageBodyReader available for deserialization purposes, the request is immediately terminated by JAX-RS and an HTTP 415 error code is returned to the client, protecting the underlying logic from parsing crashes.

###2. Filtered Retrieval & Search

Question ;

You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Answer ;

Path parameters (/sensors/123) are essentially intended to address particular and unique items or a hierarchy. Query parameters, on the other hand (/sensors?type=CO2), are always better-suited for use when searching through or filtering a collection since they function as an optional modifiers to a base collection without changing the structure of the RESTful URI. Another great advantage of using query parameters is their flexibility; they can combine several dynamic filters in any way needed (e.g., ?type=CO2&status=ACTIVE), while path parameters would require rigid, pre-defined routing paths that cannot easily scale for multi-variable searches.

##Part 4: Deep Nesting with Sub – Resources

###1. The Sub-Resource Locator Pattern

Question ;

Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

Answer ;

With the Sub-Resource Locator design pattern, huge advantages can be gained architecturally by eliminating the possibility of creating “god classes”. In any API with many resources, defining deep routes such as /sensors/{id}/readings/{rid} in one parent controller leads to an enormous file that doesn’t adhere to the Single Responsibility Principle. However, if the routing is handled via the use of a locator method that returns an object of the SensorReadingResource class, the framework handles the rest of the processing. This greatly simplifies the process because the routing will be much easier to understand, test, and maintain.

##Part 5: Advanced Error Handling, Exception Mapping & Logging

###2. Dependency Validation (422 Unprocessable Entity)

Question ;

Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

Answer ;

A 404 error in HTTP essentially means that the URI endpoint itself is invalid on the server. This situation is different from a case where a POST request from the client side contains an invalid foreign key in the JSON payload for a non-existent roomId. The request is syntactically valid but semantically invalid. Consequently, HTTP 422 (Unprocessable Entity) error code is far better suited for the given situation because it means that the server could understand the request, but could not process the business instructions within the payload due to a semantic payload reference issue.

###4. The Global Safety Net (500)

Question ;

From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Answer ;

In terms of security, providing a Java stack trace to external consumers poses a serious threat. The reason behind it is that a stack trace gives attackers a map of the inner architecture of the backend application in great detail. In other words, attackers get accurate information such as the exact file path and flaws in the process of execution and also the names and exact version numbers of the libraries used. By correlating this information with open-source data, attackers target the particular versions of libraries, making a catch-all 500 ExceptionMapper is a required global safety net.

###5. API Request & Response Logging Filters

Question ;

Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

Answer ;

Applying JAX-RS ContainerRequestFilter and ContainerResponseFilter interfaces will solve the problem of logging being a cross-cutting concern. Instead of manually putting Logger.info() in each and every resource method to ensure that all actions performed are logged appropriately, filters ensure that logging happens automatically for all HTTP transactions, including failed HTTP transactions. By doing so, we ensure that there is full observability of our application, and the Resource classes remain completely independent of logging.
