FROM openjdk:17-jdk-alpine

# 'devpilot'사용자 그룹을 생성하고 사용자를 추가하여 최소 권한을 부여
RUN addgroup --system devpilot && adduser --system --ingroup devpilot devpilot
VOLUME /tmp
ARG JAR_FILE=build/libs/devpilot-backend-0.0.1-SNAPSHOT.jar

# 작업 디렉토리 설정
WORKDIR /app
COPY ${JAR_FILE} app.jar

# 사용자 설정
USER devpilot
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.jar"]