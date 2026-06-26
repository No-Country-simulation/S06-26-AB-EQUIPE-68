# ===== STAGE 1: BUILD =====
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia o frontend (necessario para o plugin copiar para static)
COPY frontend ./frontend

# Copia o backend e faz o build
COPY backend ./backend
WORKDIR /app/backend
RUN mvn clean package -DskipTests

# ===== STAGE 2: RUNTIME =====
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia apenas o JAR gerado do stage de build
COPY --from=build /app/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
