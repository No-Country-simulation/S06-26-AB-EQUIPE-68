Write-Host "╔══════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║      BiT App - Setup do Projeto         ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════╝" -ForegroundColor Cyan

Write-Host "`n📦 Instalando dependências do backend..." -ForegroundColor Yellow
Set-Location backend
mvn clean install -DskipTests
Set-Location ..

Write-Host "`n🚀 Para iniciar o backend:" -ForegroundColor Green
Write-Host "   cd backend && mvn spring-boot:run" -ForegroundColor White

Write-Host "`n🐳 Para iniciar com Docker:" -ForegroundColor Green
Write-Host "   docker-compose up --build" -ForegroundColor White

Write-Host "`n✅ Setup concluído!" -ForegroundColor Cyan
