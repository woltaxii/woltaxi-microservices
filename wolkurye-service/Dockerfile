# WolKurye Motor Kurye & Paket Teslimat Servisi

FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install necessary packages
RUN apk add --no-cache curl tzdata

# Set timezone
ENV TZ=Europe/Istanbul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create non-root user
RUN addgroup -g 1001 -S woltaxi && \
    adduser -S woltaxi -u 1001 -G woltaxi

# Create necessary directories
RUN mkdir -p /app/logs /app/uploads/wolkurye /app/temp && \
    chown -R woltaxi:woltaxi /app

# Copy the JAR file
COPY target/wolkurye-service-*.jar app.jar

# Change ownership of the JAR file
RUN chown woltaxi:woltaxi app.jar

# Switch to non-root user
USER woltaxi

# Expose port
EXPOSE 8093

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8093/actuator/health || exit 1

# JVM optimizations for container
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:G1HeapRegionSize=16m \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -XX:+UseCompressedOops \
               -XX:+UseCompressedClassPointers \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.backgroundpreinitializer.ignore=true \
               -Dfile.encoding=UTF-8 \
               -Duser.timezone=Europe/Istanbul"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels
LABEL maintainer="WOLTAXI Development Team <dev@woltaxi.com>"
LABEL description="WolKurye Motor Kurye & Paket Teslimat Mikroservisi"
LABEL version="1.0.0"
LABEL component="wolkurye-service"
LABEL ecosystem="woltaxi-microservices"