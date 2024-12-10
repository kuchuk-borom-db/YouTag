# Use an official OpenJDK runtime as a parent image
FROM openjdk:22-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Maven executable to the image
COPY mvnw .
COPY .mvn .mvn

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Install maven and build the application
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Copy the built jar file
COPY ./target/*.jar app.jar

# Expose the port the app runs on (adjust if your app uses a different port)
EXPOSE 8080

# Allow environment variables to be passed
ENV SPRING_PROFILES_ACTIVE=dev

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]