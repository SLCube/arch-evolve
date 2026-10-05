# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY gradle ./gradle
COPY gradlew ./gradlew
COPY monolith ./monolith
COPY payment-service ./payment-service
COPY delivery-service ./delivery-service
COPY api-gateway ./api-gateway
COPY test-support ./test-support
# Windows checkout may contain CRLF. No host JDK/Gradle installation is needed.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.workers.max=2 -Dorg.gradle.jvmargs=-Xmx1024m"
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew -p monolith :module-app:bootJar -x test -x asciidoctor -x resolveAndCopyApi \
      -Pkotlin.compiler.execution.strategy=in-process && \
    ./gradlew -p payment-service bootJar -Pkotlin.compiler.execution.strategy=in-process && \
    ./gradlew -p delivery-service bootJar -Pkotlin.compiler.execution.strategy=in-process && \
    ./gradlew -p api-gateway bootJar -Pkotlin.compiler.execution.strategy=in-process

FROM eclipse-temurin:21-jre-jammy AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 app && useradd --uid 10001 --gid app app
WORKDIR /app
COPY infra/demo/logback-spring.xml /app/logback-spring.xml
USER app
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m -XX:ActiveProcessorCount=2"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM runtime AS monolith
COPY --from=build /workspace/monolith/module-app/build/libs/*.jar /app/app.jar

FROM runtime AS payment-service
COPY --from=build /workspace/payment-service/build/libs/*.jar /app/app.jar

FROM runtime AS delivery-service
COPY --from=build /workspace/delivery-service/build/libs/*.jar /app/app.jar

FROM runtime AS api-gateway
COPY --from=build /workspace/api-gateway/build/libs/*.jar /app/app.jar
