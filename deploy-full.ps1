# =========================================================
# 🚀 BiT APP - FULL AUTO DOCKER DEPLOY (PRODUCTION READY)
# =========================================================

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "==============================================="
Write-Host "🚀 BIIT APP - FULL AUTO DEPLOY"
Write-Host "==============================================="
Write-Host ""

# =========================================================
# 1. DOCKERFILE (MULTI-STAGE)
# =========================================================
Write-Host "📦 Criando Dockerfile..."

@"
# ============================
# STAGE 1 - BUILD
# ============================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ============================
# STAGE 2 - RUNTIME
# ============================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 `
  CMD wget --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java","-jar","app.jar"]
"@ | Out-File -Encoding utf8 Dockerfile

# =========================================================
# 2. DOCKER COMPOSE
# =========================================================
Write-Host "📦 Criando docker-compose.yml..."

@"
version: "3.9"

services:

  mysql:
    image: mysql:8.4
    container_name: bitapp-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: db_bitapp
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: .
    container_name: bitapp-backend
    restart: on-failure:5
    depends_on:
      mysql:
        condition: service_healthy
    env_file:
      - .env.production
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
      interval: 15s
      timeout: 5s
      retries: 5

  frontend:
    image: nginx:alpine
    container_name: bitapp-frontend
    restart: unless-stopped
    ports:
      - "3000:80"
    volumes:
      - ./frontend:/usr/share/nginx/html:ro
    depends_on:
      - backend
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:80"]
      interval: 15s
      timeout: 5s
      retries: 3

  n8n:
    image: n8nio/n8n:latest
    container_name: bitapp-n8n
    restart: unless-stopped
    ports:
      - "5678:5678"
    environment:
      N8N_BASIC_AUTH_ACTIVE: "true"
      N8N_BASIC_AUTH_USER: admin
      N8N_BASIC_AUTH_PASSWORD: admin123
      N8N_HOST: localhost
      N8N_PORT: 5678
      N8N_PROTOCOL: http
    volumes:
      - n8n_data:/home/node/.n8n

volumes:
  mysql_data:
  n8n_data:
"@ | Out-File -Encoding utf8 docker-compose.yml

# =========================================================
# 3. SPRING BOOT HEALTHCHECK (AUTO PATCH)
# =========================================================
Write-Host "⚙️ Atualizando application.properties (se necessário)..."

$appProps = "backend/src/main/resources/application.properties"

if (Test-Path $appProps) {
    Add-Content $appProps "`nmanagement.endpoints.web.exposure.include=health,info"
    Add-Content $appProps "`nmanagement.endpoint.health.show-details=always"
}

# =========================================================
# 4. BUILD + DEPLOY
# =========================================================
Write-Host "🧹 Limpando containers antigos..."

docker compose down --remove-orphans

Write-Host "🔨 Build Maven..."

mvn clean package -DskipTests

Write-Host "🐳 Build Docker..."

docker compose build --no-cache

Write-Host "🚀 Subindo ambiente..."

docker compose up -d

# =========================================================
# 5. STATUS FINAL
# =========================================================
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "==============================================="
Write-Host "✅ DEPLOY FINALIZADO COM SUCESSO"
Write-Host "==============================================="
Write-Host ""

docker ps

Write-Host ""
Write-Host "🌐 Frontend: http://localhost:3000"
Write-Host "🌐 Backend: http://localhost:8080"
Write-Host "⚙️ N8N: http://localhost:5678"
Write-Host ""
