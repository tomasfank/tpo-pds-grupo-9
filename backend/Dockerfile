# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY riva/pom.xml .
RUN mvn dependency:go-offline -q

COPY riva/src ./src
RUN mvn package -DskipTests -q

# Run stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S riva && adduser -S riva -G riva

COPY --from=builder /app/target/*.jar app.jar

USER riva

EXPOSE 8080

# Healthcheck — usa /api/health expuesto por HealthController.
# wget viene incluido vía BusyBox en eclipse-temurin:21-jre-alpine.
# start-period da margen al arranque del JVM antes de empezar a contar reintentos.
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
