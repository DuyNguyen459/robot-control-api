# Stage 1: Build
# Use Debian-based image because protobuf plugin downloads protoc binaries
# that are more reliable on glibc than Alpine/musl.
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml trước để cache dependencies
COPY pom.xml .

# Download dependencies (cache layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Tạo user không có quyền root để chạy app
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy JAR file từ builder stage
COPY --from=builder /app/target/*.jar app.jar

# Đổi quyền sở hữu
RUN chown -R appuser:appgroup /app

USER appuser

# Expose port (default 8080)
EXPOSE 8080

# Health check (use PORT env if provided)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/v3/api-docs || exit 1

# Run application, respect Render's $PORT if set
ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8080} -jar app.jar"]
