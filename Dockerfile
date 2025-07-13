# Stage 1: Build the application with Gradle
FROM gradle:8.10-jdk21 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle build --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/TriveniMgmt-0.0.1-SNAPSHOT.jar /app/store-mgmt.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/store-mgmt.jar"]
