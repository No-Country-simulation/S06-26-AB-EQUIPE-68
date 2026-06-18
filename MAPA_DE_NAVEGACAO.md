# 🗺️ MAPA DE NAVEGAÇÃO - DOCUMENTAÇÃO APP BiT

## 📍 ENCONTRE O QUE VOCÊ PRECISA RAPIDAMENTE

---

## 🔍 POR TÓPICO

### 📖 Quero Entender...

#### "...a arquitetura geral do projeto"
→ **REVISAO_COMPLETA.md** - Seção: "Arquitetura e Componentes"  
→ Tempo de leitura: 15 min

#### "...como o usuário se cadastra"
→ **src/main/java/com/bitsystem/bitapp/controller/WebController.java** - Método `onboarding()`  
→ **REVISAO_COMPLETA.md** - Seção: "Fluxo de Dados"  
→ Tempo: 10 min

#### "...como a IA gera orientações profissionais"
→ **src/main/java/com/bitsystem/bitapp/service/OrientacaoService.java** (comentários internos)  
→ **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Injeção de Dependência"  
→ Tempo: 20 min

#### "...como o check-in de saúde mental funciona"
→ **src/main/java/com/bitsystem/bitapp/service/SaudeMentalService.java** (comentários internos, seções 1-8)  
→ **REVISAO_COMPLETA.md** - Seção: "Detecção de Risco"  
→ Tempo: 25 min

#### "...como a IA é integrada"
→ **src/main/java/com/bitsystem/bitapp/client/GoogleGeminiClient.java** (300+ linhas de comentários)  
→ **application.properties** - Seção "Google Gemini"  
→ Tempo: 30 min

#### "...o modelo de dados"
→ **REVISAO_COMPLETA.md** - Seção: "Banco de Dados (Schema)"  
→ **src/main/java/com/bitsystem/bitapp/model/** (Usuario.java, HistoricoSaude.java)  
→ Tempo: 15 min

#### "...os endpoints da API"
→ **src/main/java/com/bitsystem/bitapp/controller/ApiController.java** (comentários com exemplos HTTP)  
→ Tempo: 10 min

#### "...as validações de entrada"
→ **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Jakarta Validation"  
→ **src/main/java/com/bitsystem/bitapp/dto/** (OrientacaoDto.java, SaudeDto.java)  
→ Tempo: 10 min

#### "...como fazer isso melhor (best practices)"
→ **PADROES_E_MELHORES_PRATICAS.md** - Leitura completa  
→ Tempo: 45 min

---

## 🐛 ESTOU COM UM PROBLEMA

### "API Key do Gemini não funciona"
→ **TROUBLESHOOTING.md** - Seção: "1.1 Erro: GEMINI_API_KEY não encontrada"

### "MySQL não conecta"
→ **TROUBLESHOOTING.md** - Seção: "2.1 Erro: Failed to configure a DataSource"

### "Gemini retorna 401 Unauthorized"
→ **TROUBLESHOOTING.md** - Seção: "2.1 Erro: 401 Unauthorized"

### "Gemini retorna 429 Rate Limit"
→ **TROUBLESHOOTING.md** - Seção: "2.2 Erro: 429 Too Many Requests"

### "JSON inválido da resposta Gemini"
→ **TROUBLESHOOTING.md** - Seção: "2.3 Erro: JSON inválido"

### "Coluna usuario_id não pode ser null"
→ **TROUBLESHOOTING.md** - Seção: "3.1 Erro: Column usuario_id cannot be null"

### "Email duplicado no banco"
→ **TROUBLESHOOTING.md** - Seção: "3.2 Erro: Duplicate entry"

### "Não consigo conectar ao MySQL"
→ **TROUBLESHOOTING.md** - Seção: "3.3 Erro: Can't connect to MySQL"

### "Validação não funciona"
→ **TROUBLESHOOTING.md** - Seção: "4.1 Erro: Field cannot be null"

### "Aplicação está lenta"
→ **TROUBLESHOOTING.md** - Seção: "5.1 Aplicação lenta (queries N+1)"

### "Memória vazando"
→ **TROUBLESHOOTING.md** - Seção: "5.2 Vazamento de memória"

---

## 💡 QUERO FAZER ALGO

### "Adicionar um novo endpoint REST"
1. Ler: **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Padrão MVC + REST"
2. Copiar estrutura similar em: **src/main/java/.../controller/ApiController.java**
3. Validar entrada: **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Jakarta Validation"
4. Referência: **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Injeção de Dependência"

### "Chamar Gemini com novo prompt"
1. Estudar: **src/main/java/.../client/GoogleGeminiClient.java**
2. Exemplo: **src/main/java/.../service/OrientacaoService.java**
3. Pattern: **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Records vs Classes"

### "Fazer query customizada no BD"
1. Referência: **src/main/java/.../repository/HistoricoSaudeRepository.java**
2. Como: **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Repository Pattern"

### "Adicionar nova entidade JPA"
1. Exemplo Usuario: **src/main/java/.../model/Usuario.java**
2. Exemplo Histórico: **src/main/java/.../model/HistoricoSaude.java**
3. Pattern: **REVISAO_COMPLETA.md** - Seção: "Banco de Dados"

### "Escrever testes unitários"
1. Padrão: **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Testes"
2. Exemplo completo (futuro)

### "Otimizar performance"
1. Ler: **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Performance e Otimização"
2. Debug: **TROUBLESHOOTING.md** - Seção: "5.1 Aplicação lenta"

### "Melhorar segurança"
1. Ler: **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Segurança"
2. Checklist: **application.properties** comentários sobre segurança

### "Fazer deploy em produção"
1. Ler: **REVISAO_COMPLETA.md** - Seção: "Próximos Passos"
2. Environment: **application.properties** - Seção sobre variáveis

### "Adicionar logging"
1. Padrão: **PADROES_E_MELHORES_PRATICAS.md** - Seção: "Logging e Monitoramento"

---

## 👥 PARA DIFERENTES PAPÉIS

### 👨‍💻 Desenvolvedor Junior
**Comece por:**
1. **RESUMO_FINAL_DA_REVISAO.md** (5 min)
2. **REVISAO_COMPLETA.md** - Seção "Visão Geral" (10 min)
3. **PADROES_E_MELHORES_PRATICAS.md** - Seção "Padrões de Código" (15 min)
4. Escolha um arquivo Java e leia todos os comentários (30 min)

**Tempo total:** ~1 hora

### 👨‍💼 Desenvolvedor Sênior
**Comece por:**
1. **REVISAO_COMPLETA.md** - Completo (30 min)
2. **PADROES_E_MELHORES_PRATICAS.md** - Seções "Performance" e "CI/CD" (15 min)
3. Olhe implementações específicas conforme necessário

**Tempo total:** ~1 hora

### 🏗️ Arquiteto/Tech Lead
**Comece por:**
1. **REVISAO_COMPLETA.md** - Seções "Arquitetura" e "Banco de Dados" (15 min)
2. **PADROES_E_MELHORES_PRATICAS.md** - Completo (30 min)
3. Diagramas em **REVISAO_COMPLETA.md**

**Tempo total:** ~1 hora

### 🔧 DevOps/Operações
**Comece por:**
1. **application.properties** - Com comentários (10 min)
2. **pom.xml** - Com comentários (10 min)
3. **TROUBLESHOOTING.md** - Seções 1-3 (20 min)
4. **REVISAO_COMPLETA.md** - Seção "Produção" (quando disponível)

**Tempo total:** ~45 min

### 📊 Product Manager
**Comece por:**
1. **RESUMO_FINAL_DA_REVISAO.md** (5 min)
2. **REVISAO_COMPLETA.md** - Seções "Visão Geral" e "Fluxo de Dados" (15 min)
3. **REVISAO_COMPLETA.md** - Seção "Detecção de Risco" (10 min)

**Tempo total:** ~30 min

---

## 📚 HIERARQUIA DE DOCUMENTAÇÃO

```
┌─────────────────────────────────────────┐
│  RESUMO_FINAL_DA_REVISAO.md             │ ← Comece aqui (5 min)
│  (Executive Summary)                    │
└─────────────────────────────────────────┘
              ↓
    ┌─────────┴─────────┐
    ↓                   ↓
┌─────────────────┐  ┌──────────────────────┐
│ REVISAO_        │  │ PADROES_E_MELHORES_  │
│ COMPLETA.md     │  │ PRATICAS.md          │
│ (Arquitetura)   │  │ (Engenharia)         │
└─────────────────┘  └──────────────────────┘
    ↓                   ↓
    │                   └─────────────┬──────────┐
    │                                 ↓          ↓
    ↓                           ┌──────────┐  ┌────────────┐
┌──────────────────────────┐    │ Testes   │  │ CI/CD      │
│ Arquivos .java com       │    │ (Futuro) │  │ (Futuro)   │
│ comentários detalhados   │    └──────────┘  └────────────┘
└──────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│  TROUBLESHOOTING.md                     │ ← Quando algo quebra
│  (Debug Guide)                          │
└─────────────────────────────────────────┘
```

---

## 🎯 FLUXO DE APRENDIZADO RECOMENDADO

### Dia 1: Onboarding
- [ ] Ler **RESUMO_FINAL_DA_REVISAO.md**
- [ ] Ler **REVISAO_COMPLETA.md** - Seção "Visão Geral"
- [ ] Abrir IDE e explorar estrutura de pastas
- [ ] Ler comentários de **BitappApplication.java**

### Dia 2: Modelos e DTOs
- [ ] Ler **src/main/java/.../model/Usuario.java** (com comentários)
- [ ] Ler **src/main/java/.../model/HistoricoSaude.java**
- [ ] Ler **src/main/java/.../dto/OrientacaoDto.java**
- [ ] Ler **src/main/java/.../dto/SaudeDto.java**

### Dia 3: Banco de Dados
- [ ] Ler **REVISAO_COMPLETA.md** - Seção "Banco de Dados"
- [ ] Ler **src/main/java/.../repository/UsuarioRepository.java**
- [ ] Ler **src/main/java/.../repository/HistoricoSaudeRepository.java**

### Dia 4: Integração AI
- [ ] Ler **src/main/java/.../client/GoogleGeminiClient.java** (inteiro com comentários)
- [ ] Ler **src/main/java/.../config/GoogleGeminiProperties.java**
- [ ] Ler **application.properties** - Seção Gemini

### Dia 5: Services
- [ ] Ler **src/main/java/.../service/GeminiPromptService.java**
- [ ] Ler **src/main/java/.../service/OrientacaoService.java** (inteiro com comentários)
- [ ] Ler **src/main/java/.../service/SaudeMentalService.java** (inteiro com comentários)

### Dia 6: Controllers e APIs
- [ ] Ler **src/main/java/.../controller/ApiController.java**
- [ ] Ler **src/main/java/.../controller/WebController.java**
- [ ] Testar endpoints com cURL

### Dia 7: Best Practices e Deploy
- [ ] Ler **PADROES_E_MELHORES_PRATICAS.md** - completo
- [ ] Ler **TROUBLESHOOTING.md** - para familiaridade
- [ ] Setup ambiente local

---

## 🔗 REFERÊNCIAS CRUZADAS

### GoogleGeminiClient.java
- Usado em: OrientacaoService.java, SaudeMentalService.java
- Config: GoogleGeminiProperties.java
- Properties: application.properties
- DTO de resposta: OrientacaoDto.java, SaudeDto.java

### Usuario.java
- Persistido via: UsuarioRepository.java
- Recebido em: ApiController.java (onboarding)
- Renderizado em: WebController.java (home.html)
- Relacionado com: HistoricoSaude.java (1:N)

### HistoricoSaude.java
- Persistido via: HistoricoSaudeRepository.java
- Criado em: SaudeMentalService.java
- Relacionado com: Usuario.java
- Carregado em: SaudeMentalService.java, dashboard.html

---

## 📞 PRECISO DE AJUDA?

### Documentação Rápida
- **Não sei por onde começar**: Leia **RESUMO_FINAL_DA_REVISAO.md**
- **Não entendo um arquivo**: Procure os comentários estruturados no próprio arquivo
- **Meu código não funciona**: Veja **TROUBLESHOOTING.md**
- **Quero fazer algo**: Procure em "QUERO FAZER ALGO" acima

### Pesquisa Rápida
Use Ctrl+F (Find) no seu editor:
- Procure por `/**` para encontrar documentação completa
- Procure por `SEÇÃO` para encontrar seções principais
- Procure por `DETALHES:` para entender o porquê

---

## 📊 ESTATÍSTICAS DE DOCUMENTAÇÃO

| Documento | Linhas | Tempo Leitura |
|---|---|---|
| RESUMO_FINAL_DA_REVISAO.md | 350+ | 5 min |
| REVISAO_COMPLETA.md | 500+ | 30 min |
| PADROES_E_MELHORES_PRATICAS.md | 600+ | 45 min |
| TROUBLESHOOTING.md | 400+ | 30 min |
| Código comentado (11 arquivos) | 1150+ | 1 hora |
| **TOTAL** | **3000+** | **2.5 horas** |

---

## ✅ Checklist de Onboarding

- [ ] Li **RESUMO_FINAL_DA_REVISAO.md**
- [ ] Entendo a arquitetura geral
- [ ] Consegui executar a app localmente
- [ ] Consegui testar um endpoint
- [ ] Entendo fluxo de usuário novo
- [ ] Entendo fluxo de check-in de saúde
- [ ] Consegui chamar Gemini com um prompt
- [ ] Sabia onde procurar quando tive dúvida
- [ ] Consultei TROUBLESHOOTING.md para resolver um problema
- [ ] Preparado para contribuir!

---

**Criado em:** 2026-06-12  
**Versão:** 1.0.0  
**Mantido por:** GitHub Copilot  
**Status:** ✅ Pronto para uso