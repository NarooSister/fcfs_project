# (1) 기본 이미지 정의 (빌드 단계에서만 사용)
FROM openjdk:21-jdk-slim AS builder

# (2) 작업 디렉터리 설정 (/app 폴더에서 작업 수행)
WORKDIR /app

# (3) 현재 프로젝트의 모든 파일을 Docker 이미지 내 /app 폴더로 복사
COPY . .

# (3-1) Gradle Wrapper 실행권한 설정 (필수)
RUN chmod +x ./gradlew

# (3) Gradle 빌드 수행
RUN ./gradlew clean build -x test

# (4) 런타임 환경을 위한 새 이미지 설정 (경량 이미지 사용)
FROM openjdk:21-jdk-slim

# (5) 위의 빌드된 jar 파일을 현재 이미지로 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# (5) 컨테이너가 실행될 때 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
