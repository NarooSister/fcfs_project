# 1. JDK 21을 포함한 기본 이미지 사용
FROM openjdk:21-jdk AS build

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 프로젝트 소스 코드 복사
COPY . .

# 4. 실행 권한 부여 (`gradlew` 실행 가능하도록 설정)
RUN chmod +x ./gradlew

# 5. 환경 변수 설정 (xargs 문제 방지)
ENV GRADLE_OPTS "-Dorg.gradle.daemon=false"

# 6. Gradle 빌드 실행 (테스트 제외)
RUN ./gradlew clean build -x test --no-daemon --stacktrace || true

# 7. 런타임 JDK 21 사용
FROM openjdk:21-jdk
WORKDIR /app

# 8. 빌드된 JAR 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

# 9. 컨테이너 실행 시 JAR 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
