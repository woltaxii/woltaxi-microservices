# Multi-stage Docker build for WOLTAXI Smart Vehicle Service
# Supports both Intel (amd64) and Apple Silicon (arm64) architectures

# Stage 1: Build stage
FROM maven:3.9.5-openjdk-17-slim AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage with comprehensive AI/ML libraries
FROM openjdk:17-jdk-slim

# Install system dependencies for AI/ML and autonomous vehicle libraries
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-dev \
    build-essential \
    cmake \
    pkg-config \
    libopencv-dev \
    libopencv-contrib-dev \
    libtensorflow-dev \
    libprotobuf-dev \
    protobuf-compiler \
    libeigen3-dev \
    libboost-all-dev \
    libpcl-dev \
    libyaml-cpp-dev \
    libgflags-dev \
    libgoogle-glog-dev \
    libceres-dev \
    libopenblas-dev \
    liblapack-dev \
    libatlas-base-dev \
    libhdf5-dev \
    curl \
    wget \
    unzip \
    git \
    vim \
    htop \
    && rm -rf /var/lib/apt/lists/*

# Install comprehensive Python AI/ML libraries for autonomous vehicles
RUN pip3 install --no-cache-dir \
    # Core ML frameworks
    tensorflow==2.15.0 \
    torch==2.1.0 \
    torchvision==0.16.0 \
    # Computer Vision
    opencv-python==4.8.0.76 \
    opencv-contrib-python==4.8.0.76 \
    pillow==10.0.1 \
    imageio==2.31.5 \
    scikit-image==0.21.0 \
    # Deep Learning frameworks
    keras==2.15.0 \
    onnx==1.15.0 \
    onnxruntime==1.16.3 \
    # Data processing
    numpy==1.24.3 \
    pandas==2.0.3 \
    scipy==1.11.4 \
    # Machine Learning
    scikit-learn==1.3.0 \
    xgboost==2.0.2 \
    lightgbm==4.1.0 \
    # Visualization
    matplotlib==3.7.2 \
    seaborn==0.12.2 \
    plotly==5.17.0 \
    # Autonomous vehicle specific
    pykalman==0.9.5 \
    filterpy==1.4.5 \
    # Sensor data processing
    pyserial==3.5 \
    pymavlink==2.4.41 \
    # Network and communication
    paho-mqtt==1.6.1 \
    websockets==12.0 \
    # Utilities
    tqdm==4.66.1 \
    joblib==1.3.2 \
    psutil==5.9.6 \
    h5py==3.10.0 \
    # Point cloud processing
    # open3d==0.18.0 # Commented out due to potential compatibility issues
    
# Install additional autonomous vehicle libraries
RUN pip3 install --no-cache-dir \
    # Path planning algorithms
    networkx==3.2.1 \
    shapely==2.0.2 \
    # GPS and mapping
    geopy==2.4.1 \
    folium==0.15.0 \
    # Real-time processing
    asyncio \
    multiprocessing-logging==0.3.4

# Create app directory
WORKDIR /app

# Create necessary directories
RUN mkdir -p /app/logs /app/models /app/cache /app/temp /app/certs

# Copy built application
COPY --from=builder /app/target/smart-vehicle-service-*.jar app.jar

# Copy AI model files (if available)
COPY models/ /app/models/

# Create non-root user for security
RUN groupadd -r woltaxi && useradd -r -g woltaxi woltaxi
RUN chown -R woltaxi:woltaxi /app
USER woltaxi

# Expose port
EXPOSE 8095

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8095/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-Xmx4096m -Xms2048m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0"

# Application configuration
ENV SPRING_PROFILES_ACTIVE=docker
ENV LOGGING_LEVEL_ROOT=INFO
ENV LOGGING_LEVEL_COM_WOLTAXI=DEBUG

# Smart Vehicle specific environment
ENV AUTONOMOUS_ENABLED=true
ENV AI_PROCESSING_ENABLED=true
ENV V2X_COMMUNICATION_ENABLED=true
ENV IOT_SENSORS_ENABLED=true

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="WOLTAXI Development Team"
LABEL version="1.0.0"
LABEL description="WOLTAXI Smart Vehicle Service - Future-Ready Smart Car Integration"
LABEL vendor="WOLTAXI"
LABEL component="smart-vehicle-service"
LABEL architecture="multi-arch"