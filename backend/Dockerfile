# Etapa 1: compilación
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copiar archivos necesarios para Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Dar permisos de ejecución al Maven Wrapper
RUN chmod +x mvnw

# Descargar dependencias primero
RUN ./mvnw dependency:go-offline -DskipTests

# Copiar el código y recursos del backend
COPY src src
COPY knowledge knowledge
COPY models models

# Verificar que los modelos llegaron al contenedor
RUN ls -lah models/multilingual-e5-small/

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests


# Etapa 2: ejecución
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/target/*.jar app.jar

# Render proporciona PORT; 8080 queda como respaldo
ENV PORT=8080

# Puerto utilizado por Spring Boot
EXPOSE 8080

# Iniciar Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]