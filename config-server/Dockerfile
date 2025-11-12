# 1. 실행(Runtime) 환경으로 JRE 17 이미지를 사용합니다.
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 2. docker-compose.yml에서 전달(ARG)한 JAR_FILE 경로의
#    *.jar 파일을 app.jar라는 이름으로 컨테이너에 복사합니다.
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Config Server가 사용할 설정 파일 디렉토리 복사
# ★ 중요: 실제 설정 파일들이 담긴 'config-repo' 디렉토리가 config-server 프로젝트 내에 있어야 합니다.
COPY ./config-server/src/main/resources/config-repo /config-repo

# 3. 컨테이너가 시작될 때 app.jar를 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]