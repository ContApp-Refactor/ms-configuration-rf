# Etapa 1: Build con dependencias cacheables
FROM eclipse-temurin:17-jdk-alpine AS maven

WORKDIR /opt/app

COPY .mvn/ ./.mvn
COPY mvnw ./
RUN apk add --no-cache dos2unix && \
    dos2unix ./mvnw && \
    chmod +x ./mvnw
COPY pom.xml .
# lombok.config habilita lombok.copyableAnnotations para @Qualifier: sin el
# archivo dentro de la imagen, @RequiredArgsConstructor no copia la anotacion al
# parametro del constructor y el contexto Spring falla con NoUniqueBeanDefinition.
COPY lombok.config .

# Resolve dependencies independently of source changes
RUN --mount=type=cache,id=maven-repository,target=/root/.m2/repository \
    ./mvnw dependency:go-offline -B -ntp

COPY src ./src
RUN --mount=type=cache,id=maven-repository,target=/root/.m2/repository ./mvnw  -f /opt/app/pom.xml package -Dmaven.test.skip=true -B -ntp

# Etapa 2: Imagen final liviana para producción
FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /opt/app
COPY --from=maven /opt/app/target/*-SNAPSHOT.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract \
    --layers --destination extracted


FROM eclipse-temurin:17-jre-alpine
WORKDIR /application
COPY --from=builder /opt/app/extracted/dependencies/ ./
COPY --from=builder /opt/app/extracted/spring-boot-loader/ ./
COPY --from=builder /opt/app/extracted/snapshot-dependencies/ ./
COPY --from=builder /opt/app/extracted/application/ ./
ENTRYPOINT ["java", "-jar", "application.jar"]
