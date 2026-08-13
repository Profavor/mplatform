# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache tzdata
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC"
ENV PORT=8080
EXPOSE 8080

USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar --server.port=${PORT:-8080}"]
