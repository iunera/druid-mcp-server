# Multi-stage build for Spring Boot application
FROM maven:3-eclipse-temurin-25 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src

RUN mvn clean package

# Runtime stage
FROM eclipse-temurin:25-jre

# Add MCP server metadata labels
LABEL io.modelcontextprotocol.server.name="com.iunera/druid-mcp-server"
LABEL io.modelcontextprotocol.server.description="A comprehensive Model Context Protocol (MCP) server for Apache Druid that provides AI-assisted tools, resources, and prompts for managing and analyzing Druid clusters. Built with Spring Boot and Spring AI, this server enables seamless integration between Large Language Models and Apache Druid through standardized MCP protocol, offering extensive data management, ingestion control, monitoring capabilities, and automated cluster operations for modern data analytics workflows. Developed by iunera (https://www.iunera.com)."
LABEL org.opencontainers.image.source="https://github.com/iunera/druid-mcp-server"
LABEL org.opencontainers.image.description="MCP server providing tools for Apache Druid cluster management, monitoring, and data querying"
LABEL org.opencontainers.image.vendor="iunera"
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
