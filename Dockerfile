# Multi-stage build for Spring Boot application
FROM maven:3-eclipse-temurin-24 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src

RUN mvn clean package

# Runtime stage
FROM eclipse-temurin:24-jre

# Set working directory
WORKDIR /app

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create user and group with id 1000
RUN groupadd -g 1000 appgroup 2>/dev/null || true && \
    useradd -u 1000 -g 1000 -m -s /bin/bash appuser 2>/dev/null || true  && \
    chown -R 1000:1000 /app

# Switch to the new user
USER 1000

# Copy the built JAR from build stage
COPY --from=build /app/target/druid-mcp-server-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/mcp/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
