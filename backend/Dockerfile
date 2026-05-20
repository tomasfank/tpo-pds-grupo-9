# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# Run stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S riva && adduser -S riva -G riva

COPY --from=builder /app/target/*.jar app.jar

USER riva

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
