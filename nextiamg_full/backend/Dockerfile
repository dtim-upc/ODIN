# Use the official Gradle image as a base
FROM gradle:8.5.0-jdk11 AS builder

# Set the working directory inside the container
WORKDIR /backend

# Copy the code
COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# Command to run the application
ENTRYPOINT ["gradle", "bootRun"]