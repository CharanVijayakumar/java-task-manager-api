# Java Task Manager API — New Portfolio Project

A new, minimal HTTP task API using **Java 17's built-in HTTP server**. This is not a Spring Boot implementation.

## Features
- `GET /tasks` — list tasks as JSON
- `POST /tasks?title=... ` — create a task (URL-encoded title)
- `POST /tasks/ID/complete` — mark complete

## Run
```bash
javac -d out src/TaskServer.java
java -cp out TaskServer
```
In another terminal:
```bash
curl http://localhost:8080/tasks
curl -X POST "http://localhost:8080/tasks?title=Study%20Java"
curl -X POST http://localhost:8080/tasks/1/complete
```

## Structure
`src/TaskServer.java` contains the HTTP server and in-memory store.

## Limitations
No persistence, authentication, rate limiting or robust JSON serialization. Intended for localhost demos, not deployment to the public internet. A Spring Boot/PostgreSQL version is planned.
