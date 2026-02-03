# ==================== BUILD STAGE ====================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached if gradle files don't change)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew bootJar --no-daemon -x test

# ==================== RUNTIME STAGE ====================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create uploads directory for local file storage
RUN mkdir -p /app/uploads

# Change ownership to non-root user (including uploads folder)
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
