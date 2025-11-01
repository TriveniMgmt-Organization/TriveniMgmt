# Stage 1: Build with Gradle Wrapper (Recommended)
FROM gradle:8.10-jdk21 AS builder
WORKDIR /app

# Copy Gradle wrapper and config files first (for caching)
COPY gradlew .
COPY gradle/wrapper gradle/wrapper
COPY build.gradle.kts settings.gradle.kts ./

# Download dependencies (fail gracefully if no changes)
RUN ./gradlew build --no-daemon || true

# Copy source code
COPY src ./src

# Build the application (use bootJar for Spring Boot fat JAR)
RUN ./gradlew bootJar --no-daemon

# Stage 2: Runtime (FIX: Use jdk-slim instead of jre-slim)
FROM openjdk:21-jdk-slim  
WORKDIR /app

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN addgroup --system spring && adduser --system --group spring
USER spring

# Copy the fat JAR
COPY --from=builder /app/build/libs/*.jar /app/store-mgmt.jar

EXPOSE 8080

# Optional: Health check (if Spring Boot Actuator is enabled)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
#   CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/store-mgmt.jar"]