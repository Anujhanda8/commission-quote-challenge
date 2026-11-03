# One image: Spring Boot serves the API, the mock vendor and the built React app on port 8080.
# Tests for both frontend and backend run during the build, so a failing test means no image is produced.

# Image versions; docker compose passes the values from .env.
ARG NODE_VERSION=26
ARG JAVA_VERSION=17

# --- 1. Build and test the React app ---
FROM node:${NODE_VERSION}-slim AS frontend
WORKDIR /build/frontend
# Copy only the dependency files first, so the npm install layer is cached until they change.
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm test && npm run build

# --- 2. Build and test the Spring Boot jar, with the React build as static content ---
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS backend
WORKDIR /build/backend
COPY backend/gradlew backend/settings.gradle backend/build.gradle backend/gradle.properties ./
COPY backend/gradle gradle
# Download Gradle and dependencies in their own layer, so code changes don't re-download them.
RUN ./gradlew dependencies --no-daemon > /dev/null
COPY backend/src src
# Spring Boot serves anything in static/ at the root URL, so the UI and API share one origin (no CORS needed).
COPY --from=frontend /build/frontend/dist src/main/resources/static
# "build" = compile + run all tests + package the jar.
RUN ./gradlew build --no-daemon

# --- 3. Run on a slim JRE as a non-root user ---
FROM eclipse-temurin:${JAVA_VERSION}-jre
WORKDIR /app
RUN useradd --system --no-create-home app
COPY --from=backend /build/backend/build/libs/*.jar app.jar
USER app
EXPOSE 8080
# Docker marks the container unhealthy if the app stops answering (Spring Boot Actuator health endpoint).
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
