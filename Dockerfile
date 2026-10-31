# syntax=docker/dockerfile:1.7

FROM --platform=$BUILDPLATFORM gradle:9.7.1-jdk25 AS build

WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts ./
COPY specs/openapi/main.yaml ./specs/openapi/main.yaml
COPY src/main ./src/main

ARG APP_VERSION=0.0.0-SNAPSHOT
RUN chown -R gradle:gradle /workspace
USER gradle
RUN gradle --no-daemon -PappVersion="${APP_VERSION}" bootJar -x test

FROM eclipse-temurin:25-jre

ARG APP_VERSION=0.0.0-SNAPSHOT
LABEL org.opencontainers.image.title="ProxiGaz API" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.description="ProxiGaz backend API"

RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --home-dir /app app

WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/build/libs/proxi.gaz-${APP_VERSION}.jar /app/app.jar

ENV APP_VERSION=${APP_VERSION}
EXPOSE 8080
USER 10001:10001

HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=5 \
    CMD curl --fail --silent http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
