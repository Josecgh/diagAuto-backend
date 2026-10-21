FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src
RUN mvn -B clean verify

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app \
    && mkdir -p /data && chown app:app /data /app

COPY --from=build /build/target/backend-incidents-1.0-SNAPSHOT.jar /app/app.jar

USER app
ENV DB_URL="jdbc:h2:file:/data/diagauto;DB_CLOSE_ON_EXIT=FALSE"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
