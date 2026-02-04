# Build stage
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage - optimized for Render's 512MB RAM limit
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# JVM flags optimized for low memory environments (512MB limit)
# -Xmx384m: Max heap 384MB
# -Xms256m: Initial heap 256MB
# -XX:+UseSerialGC: Low memory footprint GC
# -XX:MaxMetaspaceSize=64m: Limit metaspace
# -Xss256k: Reduce thread stack size
ENV JAVA_OPTS="-Xmx384m -Xms256m -XX:+UseSerialGC -XX:MaxMetaspaceSize=64m -Xss256k -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
