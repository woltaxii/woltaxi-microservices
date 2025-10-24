# =============================================================================
# WOLTAXI Küresel Performans ve Ödüllendirme Sistemi Docker Yapılandırması
# Global Performance & Rewards System Dockerfile
# =============================================================================

FROM openjdk:17-jdk-slim

# Metadata
LABEL maintainer="WOLTAXI Development Team"
LABEL version="1.0.0"
LABEL description="WOLTAXI Global Performance Analytics & Rewards Service"

# System packages and utilities
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Application directory
WORKDIR /app

# Copy Maven wrapper and pom.xml for dependency download
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Create non-root user for security
RUN addgroup --system woltaxi && adduser --system --group woltaxi

# Copy the JAR file
COPY target/global-performance-service-*.jar app.jar

# Change ownership
RUN chown -R woltaxi:woltaxi /app

# Switch to non-root user
USER woltaxi

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8090/actuator/health || exit 1

# Expose port
EXPOSE 8090

# JVM tuning for containers
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]