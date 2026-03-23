# Root Dockerfile to build the Java app located in RobotArmControl-main
# This ensures platforms (Render / CI) that expect a Dockerfile at repo root can build.

# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom and download dependencies
COPY RobotArmControl-main/pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source tree from subfolder
COPY RobotArmControl-main/src ./src

# Build
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/v3/api-docs || exit 1

ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8080} -jar app.jar"]
