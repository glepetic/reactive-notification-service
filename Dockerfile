FROM gradle:8.7.0-jdk-21-and-22 as build

WORKDIR /app

COPY build.gradle settings.gradle /app/
COPY src /app/src

RUN mkdir /build
RUN gradle build --no-daemon

FROM openjdk:21-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8081

ARG DEFAULT_PORT=8081
ENV APPLICATION_PORT=${APPLICATION_PORT:-$DEFAULT_PORT}
EXPOSE ${APPLICATION_PORT}

ARG DEFAULT_REDIS_HOST=localhost
ARG DEFAULT_REDIS_PORT=6379

ENV SPRING_REDIS_HOST=${SPRING_REDIS_HOST:-$DEFAULT_REDIS_HOST}
ENV SPRING_REDIS_PORT=${SPRING_REDIS_PORT:-$DEFAULT_REDIS_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]