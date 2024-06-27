# Use Eclipse Temurin as the base image
FROM eclipse-temurin:21-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper files
COPY mvnw .
COPY .mvn .mvn

# Copy the project files
COPY pom.xml .
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# The application's jar file will be named similar to 'target/healthbot-0.0.1-SNAPSHOT.jar'
# Adjust the jar file name if necessary
ARG JAR_FILE=target/*.jar

# Copy the jar file into the container
COPY ${JAR_FILE} app.jar

# Run the jar file
ENTRYPOINT ["java","-jar","/app/app.jar"]