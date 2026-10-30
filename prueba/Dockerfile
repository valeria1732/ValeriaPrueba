# Multi-stage Dockerfile para construir y ejecutar la aplicación Spring Boot
FROM gradle:8.8-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle /app/
COPY src /app/src
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /app/build/libs/*.jar app.jar
USER app
ENV PORT=8088
ENV SERVER_PORT=8088
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
