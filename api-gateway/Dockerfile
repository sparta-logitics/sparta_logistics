# 1. 실행(Runtime) 환경으로 JRE 17 이미지를 사용합니다.
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 2. docker-compose.yml에서 전달(ARG)한 JAR_FILE 경로의
#    *.jar 파일을 app.jar라는 이름으로 컨테이너에 복사합니다.
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 3. 컨테이너가 시작될 때 app.jar를 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]