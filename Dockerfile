# ============================================================
# Stage 1: Build
# ============================================================
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy Gradle configuration first
# This helps Docker cache dependencies between builds
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./

# Download dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy application source
COPY src ./src

# Build Spring Boot application
RUN ./gradlew clean bootJar --no-daemon -x test


# ============================================================
# Stage 2: Production
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && \
    adduser -S spring -G spring

# Copy application JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Give ownership to non-root user
RUN chown spring:spring /app/app.jar

USER spring:spring

# Spring Boot application port
EXPOSE 8080

# Health check
HEALTHCHECK \
    --interval=30s \
    --timeout=5s \
    --start-period=60s \
    --retries=3 \
    CMD wget --no-verbose \
        --tries=1 \
        --spider \
        http://localhost:8080/api/health/ping \
        || exit 1

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]