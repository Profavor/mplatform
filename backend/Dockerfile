# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy pom.xml and source files
COPY pom.xml ./
COPY src ./src

# Build package skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

USER appuser

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
