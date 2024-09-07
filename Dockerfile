# JDK
FROM openjdk:17-jdk-alpine

# Working directory
WORKDIR /app

# Copy the application jar file to the container
COPY target/krieger-0.0.1-SNAPSHOT.jar app.jar

# Port Exposed
EXPOSE 8080

# Command
ENTRYPOINT ["java", "-jar", "app.jar"]
