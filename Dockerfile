FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="hrabit64"

ENV TZ=Asia/Seoul

ENV SPRING_PROFILES_ACTIVE=dev
RUN mkdir -p /var/log/gabinote && \
    chmod 777 /var/log/gabinote
RUN apk add --no-cache tzdata

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]