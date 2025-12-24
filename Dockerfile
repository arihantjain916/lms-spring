#FROM openjdk:24
#WORKDIR /app
#COPY ./target/lms-0.0.1-SNAPSHOT.jar /app
#EXPOSE 8080
#CMD ["java", "-jar", "lms-0.0.1-SNAPSHOT.jar"]


# --- Stage 1: Build Stage ---
# Use an image that includes Maven and JDK 24
FROM maven:3.9.9-eclipse-temurin-24-alpine AS build
WORKDIR /app

# Copy only the pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Run Stage ---
# Use a slim JDK 24 image for the final container
FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app

# Copy the JAR from the 'build' stage
COPY --from=build /app/target/lms-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]