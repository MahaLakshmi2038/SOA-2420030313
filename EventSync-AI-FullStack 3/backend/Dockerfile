FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
RUN groupadd --system eventsync && useradd --system --gid eventsync eventsync
WORKDIR /app
COPY --from=build /workspace/target/eventsync-backend-1.0.0.jar app.jar
RUN mkdir -p /app/data && chown -R eventsync:eventsync /app
USER eventsync
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
