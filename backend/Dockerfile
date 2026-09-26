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

# COMPROBACIÓN TEMPORAL
RUN echo "===== MODELOS COPIADOS =====" && \
    find models -maxdepth 3 -type f -exec ls -lh {} \; && \
    echo "===== FIN MODELOS ====="

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]