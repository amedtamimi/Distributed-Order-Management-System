# Start with a base image containing Java runtime
FROM eclipse-temurin:17-jdk-focal

# Add Maintainer Info

# Make port 8083 available to the world outside this container
EXPOSE 8083

# Set application's JAR file
ARG JAR_FILE=target/*.jar

# Add the application's JAR to the container
COPY ${JAR_FILE} order-service.jar

# Run the jar file
ENTRYPOINT ["java","-jar","/order-service.jar"]