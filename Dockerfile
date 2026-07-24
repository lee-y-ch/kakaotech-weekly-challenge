# 1. Builder Stage
# Gradle Wrapper로 Spring Boot 실행 JAR를 생성
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

# 의존성 정보보다 변경 빈도가 낮은 Gradle 관련 파일을 먼저 복사
COPY --chmod=0755 gradlew ./gradlew
COPY gradle/ ./gradle/
COPY build.gradle settings.gradle ./

# Gradle 배포판과 프로젝트 의존성을 미리 내려받기
# 두 Gradle 실행 단계가 동일한 BuildKit 캐시를 공유
RUN --mount=type=cache,id=community-backend-gradle,target=/root/.gradle,sharing=locked \
    ./gradlew dependencies --no-daemon

# 애플리케이션 소스는 Gradle 설정과 의존성 처리 뒤에 복사
COPY src/main/ ./src/main/

# Spring Boot 실행 JAR를 만들고 이미지 레이어 단위로 추출
RUN --mount=type=cache,id=community-backend-gradle,target=/root/.gradle,sharing=locked \
    ./gradlew bootJar --no-daemon --build-cache && \
    JAR_FILE="$(find build/libs \
        -maxdepth 1 \
        -type f \
        -name '*.jar' \
        ! -name '*-plain.jar' \
        -print \
        -quit)" && \
    test -n "${JAR_FILE}" && \
    cp "${JAR_FILE}" application.jar && \
    java -Djarmode=tools \
        -jar application.jar \
        extract \
        --layers \
        --destination extracted


# 2. Runtime Stage
# 실행에 필요한 JRE와 애플리케이션 파일만 포함
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

# healthcheck 용 curl 설치
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 애플리케이션 전용 non-root 계정을 생성
RUN groupadd --system spring && \
    useradd \
        --system \
        --gid spring \
        --no-create-home \
        --shell /usr/sbin/nologin \
        spring

# 변경 가능성이 낮은 레이어부터 복사
COPY --from=builder --chown=spring:spring \
    /workspace/extracted/dependencies/ ./

COPY --from=builder --chown=spring:spring \
    /workspace/extracted/spring-boot-loader/ ./

COPY --from=builder --chown=spring:spring \
    /workspace/extracted/snapshot-dependencies/ ./

# 가장 자주 변경되는 애플리케이션 코드는 마지막에 복사
COPY --from=builder --chown=spring:spring \
    /workspace/extracted/application/ ./

USER spring:spring

# 실제 8080 포트를 외부에 여는 것은 아님 (문서 역할)
EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=50.0", \
  "-XX:MaxMetaspaceSize=256m", \
  "-XX:ReservedCodeCacheSize=128m", \
  "-Xss1m", \
  "-jar", "application.jar"]