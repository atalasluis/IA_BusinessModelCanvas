# Etapa 1: compilación
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -DskipTests

COPY src src
COPY knowledge knowledge
COPY models models

RUN ./mvnw clean package -DskipTests


# Etapa 2: ejecución
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/models models
COPY --from=build /app/knowledge knowledge

ENV PORT=8080
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]