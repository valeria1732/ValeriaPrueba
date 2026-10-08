# Multi-stage Dockerfile para construir y ejecutar la aplicación Spring Boot desde la raíz del repositorio
FROM gradle:8.8-jdk17 AS build
WORKDIR /app

# Copiar configuración y código fuente
COPY prueba/build.gradle prueba/settings.gradle /app/
COPY prueba/src /app/src

RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=build /app/build/libs/*.jar app.jar

ENV PORT=8088
ENV SERVER_PORT=8088
EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
