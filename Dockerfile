# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]