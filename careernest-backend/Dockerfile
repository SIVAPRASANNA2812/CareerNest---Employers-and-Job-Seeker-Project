# Stage 1: Build the JAR using Maven and Java 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and fetch dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build the production executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Ultra-lightweight runtime container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the compiled JAR from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose default port
EXPOSE 8081

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]