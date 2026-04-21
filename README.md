# Application Tracker

Full-stack Java/Spring Boot + React application for tracking job applications.

## Stack

- Backend: Java 17, Spring Boot, Spring Security, Spring Data JPA, MySQL, Flyway, Swagger/OpenAPI
- Frontend: React, Vite, HTML, CSS
- Auth: JWT bearer token

## Run Backend

Create the database and user:

```sql
CREATE DATABASE application_tracker;
CREATE USER 'application_tracker_app'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON application_tracker.* TO 'application_tracker_app'@'localhost';
FLUSH PRIVILEGES;
```

Start the API:

```bash
cd backend
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

## Story / PR Model

The supplied blueprint contains APP-1 through APP-167 with repeated generic story text. This repo starts with APP-1 as the baseline vertical slice. Each future story should be implemented on its own branch and merged through one pull request.

Recommended branch naming:

```text
feature/APP-001-project-bootstrap
feature/APP-002-profile-management
feature/APP-003-study-module
```
