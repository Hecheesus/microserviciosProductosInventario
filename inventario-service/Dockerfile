# Stage 1: Build
FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Add curl for healthcheck
RUN apk add --no-cache curl

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
