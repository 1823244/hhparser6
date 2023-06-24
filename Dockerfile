FROM maven:3.8.5-openjdk-17 AS builder
COPY ./ ./
RUN mvn clean package -DskipTests
FROM openjdk:17-jdk-slim
COPY --from=builder /target-baryatino/hhparser5.jar /app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "-Dserver.port=9898","/app.jar"]