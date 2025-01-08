# Start with a base image containing Java runtime
FROM eclipse-temurin:17-jdk-focal

# Add Maintainer Info
LABEL maintainer="your.email@example.com"

# Make port 8081 available to the world outside this container
EXPOSE 8081
# Set application's JAR file
ARG JAR_FILE=target/*.jar

# Add the application's JAR to the container
COPY ${JAR_FILE} product-service.jar

# Run the jar file
ENTRYPOINT ["java","-jar","/product-service.jar"]