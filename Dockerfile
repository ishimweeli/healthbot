# Use an official image as a base
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the application JAR file and properties file to the container
COPY target/predictionbot-0.0.1-SNAPSHOT.jar /app/predictionbot-0.0.1-SNAPSHOT.jar
COPY src/main/resources/application.properties /app/resources/application.properties

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "predictionbot-0.0.1-SNAPSHOT.jar"]
