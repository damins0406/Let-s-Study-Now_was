# Java 17 사용
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Gradle wrapper 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 실행 권한 부여 및 빌드
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# 포트 노출
EXPOSE 8080

# JAR 실행
CMD ["sh", "-c", "java -jar build/libs/*.jar"]