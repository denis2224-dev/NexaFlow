FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace/app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw -ntp -B -Pprod \
    -DskipTests \
    -Dskip.installnodenpm=true \
    -Dskip.npm=true \
    -Dskip.webpack=true \
    -Denforcer.skip=true \
    package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/app/target/*.jar /app/app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 8083

ENV JAVA_OPTS="-Xms256m -Xmx512m"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=10 \
    CMD curl -fsS http://127.0.0.1:8083/management/health >/dev/null || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
