FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /workspace

# Copy everything needed for a reproducible build
COPY pom.xml mvnw .
COPY .mvn .mvn
COPY src src
COPY smithy smithy
RUN mvn -B -DskipTests package -DskipSmithy -Dmaven.repo.local=/root/.m2/repository || mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
EXPOSE 8090
COPY --from=build /workspace/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
