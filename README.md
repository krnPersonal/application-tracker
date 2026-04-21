# Application Tracker

Full-stack Java/Spring Boot + React application for tracking job applications.

## Stack

- Backend: Java 17, Spring Boot, Spring Security, Spring Data JPA, MySQL, Flyway, Swagger/OpenAPI
- Frontend: React, Vite, HTML, CSS
- Auth: JWT bearer token

## Run Backend

Optional local environment file:

```bash
cp backend/.env.example backend/.env
```

Start MySQL with Docker Compose:

```bash
docker compose up -d mysql
```

The Compose service matches the backend defaults:

```text
DB_URL=jdbc:mysql://localhost:3306/application_tracker?createDatabaseIfNotExist=true&serverTimezone=UTC
DB_USERNAME=application_tracker_app
DB_PASSWORD=password
```

If you prefer a local MySQL install, create the database and user manually:

```sql
CREATE DATABASE application_tracker;
CREATE USER 'application_tracker_app'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON application_tracker.* TO 'application_tracker_app'@'localhost';
FLUSH PRIVILEGES;
```

Start the API after MySQL is healthy:

```bash
cd backend
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

## Run Frontend

Optional local environment file:

```bash
cp frontend/.env.example frontend/.env
```

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
