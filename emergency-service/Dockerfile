# =============================================================================
# WOLTAXI Emergency Service Docker Configuration
# Acil Durum Servisi Docker Yapılandırması
# =============================================================================

# Multi-stage build for optimized image size
FROM openjdk:21-jdk-slim as builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (for better caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Production stage
FROM openjdk:21-jre-slim

# Install necessary system packages for emergency service
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    iputils-ping \
    telnet \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Create app directory
RUN mkdir -p /app/logs
WORKDIR /app

# Create non-root user for security
RUN groupadd -r emergency && useradd -r -g emergency emergency

# Copy JAR file from builder stage
COPY --from=builder /app/target/emergency-service-*.jar emergency-service.jar

# Copy configuration files
COPY --from=builder /app/src/main/resources/application.yml application.yml

# Create log directory and set permissions
RUN chown -R emergency:emergency /app && \
    chmod 755 /app && \
    chmod 644 /app/emergency-service.jar

# Switch to non-root user
USER emergency

# Environment variables for configuration
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SERVER_PORT=8091

# Health check for container orchestration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8091/actuator/health || exit 1

# Expose port
EXPOSE 8091

# Add labels for better container management
LABEL maintainer="WolTaxi Emergency Team <emergency@woltaxi.com>"
LABEL version="1.0.0"
LABEL description="WolTaxi Emergency Service - Critical Safety System"
LABEL service.name="emergency-service"
LABEL service.type="microservice"
LABEL service.priority="critical"

# Entry point with JVM optimization for containers
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar emergency-service.jar"]