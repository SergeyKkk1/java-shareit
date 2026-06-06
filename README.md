# java-shareit

Application for sharing items between users. The project is split into two independent
Spring Boot applications inside a single multi-module Maven project; they talk to each
other over REST.

## Modules

| Module            | Port | Responsibility                                                                 |
|-------------------|------|--------------------------------------------------------------------------------|
| `shareit-gateway` | 8080 | User-facing controllers and request/DTO validation that does not need the DB.  |
| `shareit-server`  | 9090 | All business logic, persistence (JPA), mappers, and DB-dependent validation.   |

The gateway validates incoming requests and forwards them to the server via `RestTemplate`
(see `client/BaseClient`). The server owns the database and all domain logic.

## Features

* Users, items, bookings and comments (as before).
* **Item requests** (`/requests`):
  * `POST /requests` — create a request (body: `description`).
  * `GET /requests` — the caller's own requests, newest first, each with the answering items.
  * `GET /requests/all` — requests created by other users, newest first.
  * `GET /requests/{requestId}` — a single request with its answers (any user).
* When creating an item a user may pass an optional `requestId`; the item then appears
  in that request's list of answers (`itemId`/`name`/`ownerId`).

## Build

A single command builds both modules:

```bash
mvn clean package
```

Quality gate:

```bash
mvn verify -P check,coverage
```

## Run

With Docker (gateway + server + PostgreSQL):

```bash
mvn clean package
docker compose up
```

Locally without Docker — the server uses an in-memory H2 database under the `test` profile:

```bash
java -jar server/target/shareit-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=test
java -jar gateway/target/shareit-gateway-0.0.1-SNAPSHOT.jar
```

The gateway reads the server location from `shareit-server.url`
(env `SHAREIT_SERVER_URL`, default `http://localhost:9090`).
