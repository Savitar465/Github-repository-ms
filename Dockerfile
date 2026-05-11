FROM maven:3.9.11-eclipse-temurin-25 AS build
LABEL org.opencontainers.image.title="Github-repository-ms" \
	org.opencontainers.image.description="Microservicio de repositorios y metadatos" \
	org.opencontainers.image.vendor="Githubx" \
	org.opencontainers.image.url="https://github.com/Savitar465/Github-repository-ms" \
	org.opencontainers.image.source="https://github.com/Savitar465/Github-repository-ms" \
	org.opencontainers.image.documentation="https://github.com/Savitar465/Github-repository-ms/blob/main/README.md" \
	org.opencontainers.image.authors="Savitar465"
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
