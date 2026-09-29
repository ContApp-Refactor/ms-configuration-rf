# Etapa 1: Build con dependencias cacheables
FROM eclipse-temurin:17-jdk-alpine AS maven

WORKDIR /opt/app

COPY .mvn ./.mvn
COPY mvnw ./
RUN apk add --no-cache dos2unix && \
    dos2unix ./mvnw && \
    chmod +x ./mvnw
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -f /opt/app/pom.xml package -DskipTests

# Etapa 2: Imagen final liviana para producción
FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /opt/app
COPY --from=maven /opt/app/target/*-SNAPSHOT.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract


FROM eclipse-temurin:17-jre-alpine
WORKDIR /application
COPY --from=builder /opt/app/dependencies/ ./
COPY --from=builder /opt/app/spring-boot-loader/ ./
COPY --from=builder /opt/app/snapshot-dependencies/ ./
COPY --from=builder /opt/app/application/ ./
ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]

