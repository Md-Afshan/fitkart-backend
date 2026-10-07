# Multi-stage Dockerfile for FitKart Spring Boot Backend (Java 21)

# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Ensure mvnw script is executable
RUN chmod +x mvnw

# Download dependencies (go-offline)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build executable JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests -B && rm -f target/*.jar.original

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose default port (Render overrides via PORT environment variable)
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
