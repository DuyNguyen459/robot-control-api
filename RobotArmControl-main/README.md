# Robot Arm Control System

Comprehensive monorepo for a robot arm control platform with:
- Spring Boot backend API
- React web admin frontend
- Flutter mobile app
- Python AI camera edge client
- Docker-based deployment

## 1) Repository Overview

Top-level modules:
- `src/` -> Spring Boot backend source
- `robot-control-system-frontend/` -> React + Vite web admin
- `mo_src/` -> Flutter mobile app
- `ai-camera/` -> Python AI camera controller (MediaPipe + WebSocket)
- `docker-compose.yml` + `Dockerfile` -> containerized backend + Postgres
- `.github/workflows/docker-build-push.yml` -> CI build/push Docker image to GHCR

## 2) High-Level Architecture

- Backend (Spring Boot) is the central API, auth, business logic, and integration hub.
- Postgres stores relational data (users, factories, areas, hubs, devices, etc.).
- Astra Cassandra stores robot logs/telemetry query models.
- Web frontend consumes REST APIs for admin operations.
- Mobile app consumes REST APIs for operator/admin runtime workflows.
- AI camera client streams hand-derived robot angles over WebSocket (`/ws/robot-control`) and reacts to start/stop commands.

## 3) Backend (Spring Boot)

### 3.1 Tech Stack

From `pom.xml`:
- Java 21
- Spring Boot 4.0.2
- Spring Web MVC
- Spring Validation
- Spring Data JPA
- Spring Data Cassandra
- Spring Security
- Spring WebSocket
- JWT (`jjwt`)
- SpringDoc OpenAPI (Swagger UI)
- PostgreSQL driver
- Lombok

### 3.2 Backend Runtime Config

`src/main/resources/application.properties`:
- server port: 8080
- Postgres datasource configured
- JWT secret/expiration configured
- Cassandra/Astra settings configured

Astra helper files:
- `src/main/resources/application-astra.example.yml`
- `src/main/resources/schema-astra.cql`
- `src/main/resources/secure-connect-robot-control-log.zip`

### 3.3 Core API Groups

Auth:
- `POST /api/auth/login`
- `POST /api/auth/register`

Users:
- `/api/users/**` (CRUD + role/status updates)

Factory hierarchy:
- `/api/factories/**`
- `/api/factories/{factoryId}/areas`
- `/api/areas/{areaId}/hubs`
- `/api/hubs/{hubId}/devices`
- `/api/areas/{areaId}` + `/status`
- `/api/hubs/{hubId}` + `/status`
- `/api/devices/{deviceId}` + `/status`

Robot runtime/control:
- `/api/robots/**`
- `/api/control-sessions/**`
- `/api/camera/**`

Gesture and command:
- `/api/gestures/**`
- `/api/gesture-mappings/**`
- `/api/robot-commands/**`

Logging:
- `/api/logs/**`
  - ingest one
  - ingest batch
  - logs by robot/day
  - logs by session
  - latest status
  - alerts
- `/api/control-logs/**`

Telemetry:
- `/api/telemetry/**`

Health/doc:
- Swagger/OpenAPI at `/swagger-ui.html` and `/v3/api-docs`

### 3.4 Security and Roles

From `SecurityConfig`:
- Public:
  - `/api/auth/**`
  - Swagger endpoints
- Admin-only:
  - `/api/users/**`
  - `/api/admin/**`
  - `DELETE /api/**`
- Admin + Operator:
  - mutation endpoints in gestures/commands/runtime/control
- Logs (`/api/logs/**`): ADMIN, OPERATOR, VIEWER
- Most GET API endpoints: authenticated
- WebSocket path `/ws/**` is permitted and protected by handshake interceptor logic

## 4) Web Frontend (`robot-control-system-frontend`)

### 4.1 Tech Stack

From `package.json`:
- React 19
- React Router DOM 7
- Vite 7
- TailwindCSS 3
- Axios
- Ant Design
- MediaPipe JS packages present in dependencies

### 4.2 Dev Proxy

From `vite.config.js`:
- `/api` proxied to `https://robot-control-api-1.onrender.com`
- `/ws` proxied to `wss://robot-control-api-1.onrender.com`

### 4.3 Main Capabilities

Based on existing frontend README and source:
- Login flow
- Admin protected routes
- CRUD management screens:
  - Factories
  - Areas
  - Hubs
  - Devices
- Dashboard exists (currently mostly mock/stub data)

## 5) Mobile App (`mo_src`)

### 5.1 Tech Stack

From `pubspec.yaml`:
- Flutter SDK
- `http`
- `shared_preferences`

### 5.2 Current Runtime Behavior

From `mo_src/lib/main.dart` and screens:
- Dark theme app
- Session restoration and expiration handling
- Role-based home:
  - Admin -> admin main screen
  - Non-admin -> operator/main screen

### 5.3 Operator Navigation (current)

Bottom tabs currently mapped in order:
1. Devices
2. Factory
3. Control
4. Robot Log
5. Profile

### 5.4 Mobile Integrations

- Auth + session persistence
- Factory/Area/Hub/Device APIs
- Robot log APIs:
  - `/api/logs/robots/{robotId}/today`
  - `/api/logs/robots/{robotId}/alerts`
  - `/api/logs/sessions/{sessionId}`
  - `/api/logs/robots/{robotId}/latest-status`

## 6) AI Camera Edge Client (`ai-camera`)

### 6.1 Dependencies

From `ai-camera/requirements.txt`:
- opencv-python
- mediapipe
- websocket-client
- numpy
- requests
- Flask

### 6.2 Behavior

From `ai-camera/ai_camera.py`:
- Uses MediaPipe Hand Landmarker model
- Maps hand/finger signals to 6-DOF joint angle control
- Connects to backend WebSocket (`/ws/robot-control`)
- Listens for camera start/stop commands from server
- Sends `ai_angles` payload to backend in near real-time

## 7) Docker and Deployment

### 7.1 Docker Compose

From `docker-compose.yml`:
- Service `app` (Spring Boot container)
- Service `db` (Postgres 16)
- Exposed ports:
  - 8080 (backend)
  - 5432 (Postgres)
- Health checks enabled
- Uses environment variables for DB credentials, JWT secret, Astra token, SCB path

### 7.2 Dockerfile

Multi-stage build:
1. Maven build image compiles jar
2. Temurin JRE runtime image runs app as non-root user

### 7.3 CI/CD

From `.github/workflows/docker-build-push.yml`:
- Trigger: push to main/master, tags `v*`, PR to main/master
- Builds multi-arch images (amd64, arm64)
- Pushes to GitHub Container Registry on non-PR events

## 8) Environment Variables

### 8.1 Basic (`.env.example`)
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `SPRING_PROFILES_ACTIVE`

### 8.2 Additional variables seen in runtime configs
- `ASTRA_TOKEN`
- `SCB_PATH`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `ASTRA_KEYSPACE`
- `ASTRA_CLIENT_ID`
- `ASTRA_CLIENT_SECRET`
- `ASTRA_SCB_PATH`

### 8.3 Mobile build-time defines (optional)
- `API_BASE_URL`
- `API_ACCESS_TOKEN`

### 8.4 AI camera env vars
- `ROBOT_API_BASE_URL`
- `ROBOT_WS_URL`

## 9) Local Development Guide

### 9.1 Backend only (local Java)

Requirements:
- Java 21
- Maven Wrapper
- Postgres running locally

Commands:
- Windows: `mvnw.cmd spring-boot:run`
- Or package: `mvnw.cmd clean package -DskipTests`

Default backend URL:
- `http://localhost:8080`

Swagger:
- `http://localhost:8080/swagger-ui.html`

### 9.2 Backend + DB with Docker Compose

- `docker compose up --build`

### 9.3 Web frontend

- `cd robot-control-system-frontend`
- `npm install`
- `npm run dev`

### 9.4 Mobile app

- `cd mo_src`
- `flutter pub get`
- `flutter analyze`
- `flutter run`

Build APK:
- `flutter build apk --debug`

### 9.5 AI camera

- `cd ai-camera`
- `pip install -r requirements.txt`
- `python ai_camera.py`

## 10) Database Notes

- Relational domain data is managed in Postgres.
- Robot log query models are denormalized into Cassandra tables as defined in `schema-astra.cql`.
- Key Cassandra tables include:
  - `robot_logs_by_robot_day`
  - `robot_logs_by_session`
  - `robot_logs_by_robot_day_type`
  - `robot_alerts_by_robot_day`
  - `robot_latest_status`

## 11) Known Gaps / TODOs Observed in Source

- Some controller methods are scaffolded with TODO placeholders.
- Mixed endpoint styles exist across legacy/new runtime routes.
- Frontend dashboard/control/settings areas include partial stub content.
- API base URLs in web and mobile are currently hardcoded/proxied for a deployed backend; adjust for local environments as needed.

## 12) Quick Troubleshooting

### Android build fails with No Android SDK found
- Ensure SDK is installed.
- Point Flutter explicitly:
  - `flutter config --android-sdk "D:\\Android SDK"` (example path)
- Accept licenses:
  - `flutter doctor --android-licenses`
- Recheck:
  - `flutter doctor -v`

### API auth errors in frontend/mobile
- Verify backend is reachable.
- Confirm JWT token exists and has not expired.
- Check role permissions in backend `SecurityConfig`.

### CORS or WebSocket issues
- For web local dev, ensure Vite proxy is active.
- Verify backend WebSocket endpoint is reachable at `/ws/robot-control`.

---

If you want, this README can be split into module-specific docs next:
- `docs/backend.md`
- `docs/mobile.md`
- `docs/frontend.md`
- `docs/ai-camera.md`
- `docs/deployment.md`
