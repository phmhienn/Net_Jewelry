FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 jewelry \
    && useradd --uid 10001 --gid jewelry --create-home jewelry
WORKDIR /app
COPY --from=build /build/target/jewelry-store-0.0.1-SNAPSHOT.jar /app/app.jar
RUN mkdir -p /app/uploads && chown jewelry:jewelry /app/uploads
USER jewelry
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
