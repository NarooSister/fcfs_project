#!/bin/bash

# 빌드 실패 시 스크립트 종료
set -e

# 서비스 디렉토리 배열 정의
SERVICES=("eureka-server" "gateway" "order-service" "ticket-service" "user-service")

echo "Starting Gradle build for all services..."
for SERVICE in "${SERVICES[@]}"; do
  echo "Building JAR for ${SERVICE}..."
  (cd $SERVICE && ./gradlew clean build -x test)
done
echo "Gradle build completed."

# Docker 이미지 빌드
for SERVICE in "${SERVICES[@]}"; do
  JAR_FILE=build/libs/${SERVICE}-0.0.1-SNAPSHOT.jar

  echo "Building Docker image for ${SERVICE}..."
  docker build --build-arg JAR_FILE=${JAR_FILE} -t "${SERVICE}:latest" ./${SERVICE}
done

echo "All Docker images built successfully."

# Docker Compose 실행
echo "Starting Docker Compose..."
docker-compose up --build -d
