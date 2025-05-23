FROM openjdk:17-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=build/libs/devpilot-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]