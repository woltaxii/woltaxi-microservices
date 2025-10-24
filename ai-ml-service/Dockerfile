# =============================================================================
# WOLTAXI AI/ML Service Docker Configuration
# Yapay Zeka ve Makine Öğrenmesi Servisi Docker Yapılandırması
# Cross-Platform Support: Windows, macOS, Linux, ARM64 (Apple Silicon)
# =============================================================================

# Multi-stage build for optimized cross-platform image
FROM --platform=$BUILDPLATFORM openjdk:21-jdk-slim as builder

# Build arguments for cross-platform support
ARG TARGETPLATFORM
ARG BUILDPLATFORM

# Set working directory
WORKDIR /app

# Install build dependencies based on platform
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    maven \
    git \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

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

# Production stage with multi-platform support
FROM --platform=$TARGETPLATFORM openjdk:21-jre-slim

# Install AI/ML dependencies and system packages
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    iputils-ping \
    telnet \
    netcat-openbsd \
    python3 \
    python3-pip \
    python3-dev \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Install Python ML libraries for additional AI capabilities
RUN pip3 install --no-cache-dir \
    numpy \
    pandas \
    scikit-learn \
    opencv-python-headless \
    pillow \
    requests

# Create app directory and necessary subdirectories
RUN mkdir -p /app/logs \
    /app/models \
    /app/temp \
    /app/uploads \
    /app/cache

# Create non-root user for security (cross-platform compatible)
RUN groupadd -r aiml && useradd -r -g aiml -d /app -s /bin/bash aiml

# Copy JAR file from builder stage
COPY --from=builder /app/target/ai-ml-service-*.jar /app/ai-ml-service.jar

# Copy configuration files
COPY --from=builder /app/src/main/resources/application.yml /app/application.yml

# Set proper permissions for cross-platform compatibility
RUN chown -R aiml:aiml /app && \
    chmod 755 /app && \
    chmod 644 /app/ai-ml-service.jar && \
    chmod 755 /app/models /app/logs /app/temp /app/uploads /app/cache

# Switch to non-root user
USER aiml

# Set working directory
WORKDIR /app

# Environment variables for cross-platform optimization
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx2048m -Xms1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+UseContainerSupport"
ENV SERVER_PORT=8094
ENV AI_ML_MODELS_PATH=/app/models
ENV AI_ML_CACHE_PATH=/app/cache
ENV AI_ML_TEMP_PATH=/app/temp

# TensorFlow specific optimizations for different platforms
ENV TF_CPP_MIN_LOG_LEVEL=2
ENV TF_ENABLE_ONEDNN_OPTS=1

# Platform-specific optimizations
ENV OPENCV_LOG_LEVEL=ERROR
ENV PYTHONPATH=/usr/local/lib/python3.9/site-packages

# Expose port
EXPOSE 8094

# Health check for container orchestration (cross-platform)
HEALTHCHECK --interval=30s --timeout=15s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8094/actuator/health || exit 1

# Add comprehensive labels for better container management
LABEL maintainer="WOLTAXI AI/ML Team <ai-ml@woltaxi.com>"
LABEL version="1.0.0"
LABEL description="WOLTAXI AI/ML Service - Comprehensive Machine Learning Platform"
LABEL service.name="ai-ml-service"
LABEL service.type="microservice"
LABEL service.priority="high"
LABEL platform.support="linux/amd64,linux/arm64,windows,darwin"
LABEL security.level="enterprise"
LABEL ai.frameworks="tensorflow,opencv,sklearn,weka"
LABEL ai.capabilities="computer-vision,nlp,machine-learning,predictive-analytics"

# Volume mounts for persistent data
VOLUME ["/app/models", "/app/logs", "/app/cache"]

# Entry point with platform-aware JVM optimization
ENTRYPOINT ["sh", "-c", "\
    echo 'Starting WOLTAXI AI/ML Service on platform: '$(uname -m) && \
    echo 'Java version: '$(java -version 2>&1 | head -n 1) && \
    echo 'Available memory: '$(free -h | grep Mem | awk '{print $2}') && \
    echo 'CPU info: '$(nproc)' cores' && \
    java $JAVA_OPTS \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -Dserver.port=$SERVER_PORT \
    -Dwoltaxi.aiml.models.storage-path=$AI_ML_MODELS_PATH \
    -Dwoltaxi.aiml.cache.path=$AI_ML_CACHE_PATH \
    -Dwoltaxi.aiml.temp.path=$AI_ML_TEMP_PATH \
    -jar ai-ml-service.jar"]

# Security hardening
USER aiml
WORKDIR /app

# Final platform verification
RUN echo "AI/ML Service Docker image built successfully for platform: $(uname -m)"