# 🐛 GUIA DE TROUBLESHOOTING - APP BiT

## 1. PROBLEMAS DE INICIALIZAÇÃO

### 1.1 Erro: "GEMINI_API_KEY não encontrada"

**Erro:**
```
java.lang.IllegalStateException: API Key do Gemini não configurada
```

**Causas:**
- Variável de ambiente `GEMINI_API_KEY` não definida
- Arquivo `.env` não carregado
- IDE não atualizou variáveis de ambiente

**Soluções:**

```bash
# 1. Verificar variável de ambiente
echo $GEMINI_API_KEY  # Linux/Mac
echo %GEMINI_API_KEY% # Windows CMD
$env:GEMINI_API_KEY   # Windows PowerShell

# 2. Definir no terminal
export***REMOVED***
set***REMOVED***
$env:GEMINI_API_KEY="AIzaSyD..."    # Windows PowerShell

# 3. Arquivo .env (IDE específico)
# IntelliJ IDEA: File → Settings → Build, Execution, Deployment → 
#   Gradle/Maven → Runner → VM options
#   -DGEMINI_API_KEY=AIzaSyD...

# 4. Restartar IDE/Terminal
```

### 1.2 Erro: "Failed to configure a DataSource"

**Erro:**
```
Failed to configure a DataSource: 'url' attribute is not specified and 
no embedded datasource could be auto-configured.
```

**Causas:**
- MySQL não está rodando
- URL de conexão errada
- MySQL Port (3306) bloqueada

**Soluções:**

```bash
# 1. Verificar se MySQL está rodando
mysql -u root -p  # Linux/Mac
# ou verificar Windows Services

# 2. Iniciar MySQL
sudo systemctl start mysql              # Linux
brew services start mysql              # Mac
net start MySQL80                       # Windows (admin console)

# 3. Verificar porta
netstat -tuln | grep 3306              # Linux/Mac
netstat -ano | findstr :3306            # Windows

# 4. Se necessário, alterar porta em application.properties
spring.datasource.url=jdbc:mysql://localhost:3307/db_bitapp
```

### 1.3 Erro: "Unknown database 'db_bitapp'"

**Erro:**
```
Unknown database 'db_bitapp'; nested exception is com.mysql.cj.jdbc.exceptions.
MySQLSyntaxErrorException: Unknown database 'db_bitapp'
```

**Causas:**
- Banco não foi criado (createDatabaseIfNotExist=true às vezes falha)
- Permissões insuficientes do usuário MySQL

**Soluções:**

```bash
# 1. Conectar ao MySQL
mysql -u root -p

# 2. Criar banco manualmente
CREATE DATABASE db_bitapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. Verificar banco foi criado
SHOW DATABASES;

# 4. Se usar usuário específico, conceder permissões
GRANT ALL PRIVILEGES ON db_bitapp.* TO 'bitapp_user'@'localhost';
FLUSH PRIVILEGES;

# 5. Reiniciar aplicação
```

---

## 2. PROBLEMAS DE CONEXÃO COM GEMINI

### 2.1 Erro: "401 Unauthorized - Invalid API Key"

**Erro:**
```
com.google.ai.generativelanguage.v1beta2.GenerateTextResponse 
[Error: 401 Unauthorized - Invalid API Key]
```

**Causas:**
- API Key inválida ou expirada
- API Key errada (copiou sem querer um espaço)
- API Key não tem permissões

**Soluções:**

```bash
# 1. Verificar se a chave está sendo carregada
# Adicionar log temporário em GoogleGeminiClient:
logger.debug("Using API Key: {}", properties.getApiKey().substring(0, 10) + "...");

# 2. Regenerar chave
# Acesse: https://makersuite.google.com/app/apikey
# Delete a chave existente e crie uma nova

# 3. Verificar se projeto no Google Cloud tem Generative Language API habilitada
# https://console.cloud.google.com/apis/library/generativelanguage.googleapis.com

# 4. Testar com cURL
curl -X POST \
  "https://generativelanguage.googleapis.com/v1beta2/models/gemini-pro:generateMessage?key=YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"author": "user", "content": [{"type": "text", "text": "Hello"}]}
    ]
  }'
```

### 2.2 Erro: "429 Too Many Requests - Rate Limit Exceeded"

**Erro:**
```
com.google.ai.generativelanguage.v1beta2.GenerateTextResponse 
[Error: 429 Too Many Requests]
```

**Causas:**
- Seu IP fez muitas requisições (rate limit)
- Free tier do Gemini tem cota limitada
- Múltiplas instâncias da app fazendo requisições

**Soluções:**

```java
// 1. Implementar retry com backoff exponencial
private static final int MAX_RETRIES = 3;
private static final int INITIAL_WAIT_MS = 1000;

public String generateTextWithRetry(String prompt) {
  int attempt = 0;
  while (attempt < MAX_RETRIES) {
    try {
      return geminiClient.generateText(prompt);
    } catch (HttpClientErrorException.TooManyRequests e) {
      attempt++;
      if (attempt >= MAX_RETRIES) throw e;
      
      long waitTime = INITIAL_WAIT_MS * (long) Math.pow(2, attempt - 1);
      logger.warn("Rate limit. Tentativa {} em {}ms", attempt, waitTime);
      Thread.sleep(waitTime);
    }
  }
  return null;
}

// 2. Adicionar cache para respostas frequentes
@Cacheable("gemini-responses")
public String generateCachedText(String prompt) {
  return generateTextWithRetry(prompt);
}

// 3. Usar fila para processar requisições sequencialmente
@Async
public CompletableFuture<String> generateTextAsync(String prompt) {
  return CompletableFuture.completedFuture(generateTextWithRetry(prompt));
}

// 4. Monitorar cota
// Google Cloud Console → Quotas & System Limits
```

### 2.3 Erro: JSON inválido na resposta Gemini

**Erro:**
```
com.fasterxml.jackson.core.JsonParseException: 
Unrecognized token 'Lorem': was expecting ('true', 'false' or 'null')
```

**Causas:**
- Gemini retornou texto simples em vez de JSON
- Prompt não forçava JSON estruturado
- Response foi truncada

**Soluções:**

```java
// 1. Melhorar prompt para garantir JSON
String userPrompt = String.format("""
  Você DEVE retornar APENAS um JSON válido, sem prefixo ou sufixo.
  ...
  Retorne estritamente este formato JSON:
  {
    "gapPercentual": número,
    "gapItens": ["item1", "item2"],
    ...
  }
  """, params);

// 2. Adicionar tratamento fallback
try {
  return objectMapper.readValue(geminiResult, OrientacaoDto.Response.class);
} catch (JsonParseException e) {
  logger.warn("Resposta inválida, tentando extrair JSON...");
  
  // Tentar extrair JSON entre chaves
  Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
  Matcher matcher = pattern.matcher(geminiResult);
  
  if (matcher.find()) {
    String jsonString = matcher.group();
    return objectMapper.readValue(jsonString, OrientacaoDto.Response.class);
  }
  throw new IllegalStateException("JSON inválido: " + geminiResult);
}

// 3. Logar resposta completa para debugging
logger.debug("Gemini response: {}", geminiResult);
```

---

## 3. PROBLEMAS DE BANCO DE DADOS

### 3.1 Erro: "Column 'usuario_id' cannot be null"

**Erro:**
```
java.sql.SQLIntegrityConstraintViolationException: Column 'usuario_id' 
cannot be null
```

**Causas:**
- `Usuario` não foi salvo antes de criar `HistoricoSaude`
- `usuario_id` foi setado como null
- Relacionamento FK não foi configurado

**Soluções:**

```java
// ❌ Errado: criar histórico sem usuário
HistoricoSaude historico = new HistoricoSaude();
historico.setUsuario(null); // Causa FK constraint violation!
saudeRepository.save(historico);

// ✅ Correto: buscar usuário existente
Usuario usuario = usuarioRepository.findById(request.usuarioId())
  .orElseThrow(() -> new UsuarioNaoEncontradoException(request.usuarioId()));

HistoricoSaude historico = HistoricoSaude.builder()
  .usuario(usuario)  // FK preenchido
  .humor(request.humor())
  .notaSemanal(request.notaSemanal())
  .build();
saudeRepository.save(historico);
```

### 3.2 Erro: "Duplicate entry for key 'email'"

**Erro:**
```
java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 
'alice@example.com' for key 'tb_usuarios.email'
```

**Causas:**
- Email já existe no banco
- Validação de unicidade não foi feita

**Soluções:**

```java
// ✅ Validação antes de salvar
Optional<Usuario> existente = usuarioRepository.findByEmail(usuario.getEmail());
if (existente.isPresent()) {
  throw new IllegalArgumentException("Email já cadastrado");
}

usuarioRepository.save(usuario);

// Ou adicionar validação JPA:
@Entity
public class Usuario {
  @Column(unique = true)
  private String email;  // BD garante unicidade
}
```

### 3.3 Erro: "Can't connect to MySQL server"

**Erro:**
```
java.sql.SQLException: Can't connect to MySQL server on 
'localhost:3306' (Connection refused)
```

**Causas:**
- MySQL não está rodando
- Porta 3306 está bloqueada
- Hostname errado

**Soluções:**

```bash
# 1. Verificar status MySQL
sudo systemctl status mysql           # Linux
brew services list                    # Mac
Get-Service -Name MySQL80             # Windows

# 2. Iniciar MySQL
sudo systemctl start mysql
brew services start mysql
net start MySQL80

# 3. Verificar conectividade
ping localhost
telnet localhost 3306

# 4. Se em Docker, verificar rede
docker ps  # Verif if MySQL container está rodando
docker logs mysql-container
docker network inspect bridge
```

---

## 4. PROBLEMAS DE VALIDAÇÃO

### 4.1 Erro: "Field email cannot be null"

**Erro:**
```
Validation failed for object='usuarioRequest'. 
Error count: 1
FieldError{message='Email é obrigatório'}
```

**Causas:**
- Campo faltando na requisição
- Cliente não validando antes de enviar
- @Valid não está ativo

**Soluções:**

```java
// ✅ Garantir @Valid no controller
@PostMapping("/usuario")
public ResponseEntity<Usuario> criar(
  @RequestBody @Valid UsuarioRequest request) {  // @Valid ativa validação
  // ...
}

// ✅ Mensagens de erro customizadas
public record UsuarioRequest(
  @NotBlank(message = "Nome é obrigatório e não pode ter espaços")
  String nome,
  
  @NotBlank(message = "Email é obrigatório")
  @Email(message = "Email deve ser válido (ex: user@domain.com)")
  String email
) {}

// ✅ Testar requisição com cURL
curl -X POST http://localhost:8080/api/usuario \
  -H "Content-Type: application/json" \
  -d '{"nome": "", "email": "invalid"}'
```

---

## 5. PROBLEMAS DE PERFORMANCE

### 5.1 Aplicação lenta (queries N+1)

**Sintoma:**
- POST /api/saude demora 5+ segundos
- Múltiplas queries SELECT no log

**Causas:**
- Lazy loading com loop (N+1 queries)
- Sem índices nas colunas de busca

**Debug:**

```properties
# Ativar SQL logging
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# Output:
# SELECT * FROM tb_usuarios WHERE id = 1
# SELECT * FROM tb_historico_saude WHERE usuario_id = 1  (N+1 problema)
```

**Soluções:**

```java
// ❌ Problema: N+1 queries
List<Usuario> usuarios = usuarioRepository.findAll();
for (Usuario u : usuarios) {
  List<HistoricoSaude> saude = saudeRepository
    .findByUsuarioIdOrderByDataRegistroDesc(u.getId());  // N queries!
}

// ✅ Solução 1: Eager fetch
@Query("SELECT u FROM Usuario u JOIN FETCH u.historico WHERE u.id = ?1")
Optional<Usuario> findByIdEager(Long id);

// ✅ Solução 2: Adicionar índice
@Entity
@Table(name = "tb_historico_saude", 
  indexes = @Index(name = "idx_usuario_id", columnList = "usuario_id"))
public class HistoricoSaude { ... }

// ✅ Solução 3: Cache
@Cacheable("historico-saude")
public List<HistoricoSaude> getHistoricoCache(Long usuarioId) {
  return saudeRepository.findByUsuarioIdOrderByDataRegistroDesc(usuarioId);
}
```

### 5.2 Vazamento de memória

**Sintoma:**
- Aplicação fica mais lenta com o tempo
- Heap memory cresce constantemente

**Causas:**
- Cache não expirando
- Conexões não fechando
- Listeners não deregistrando

**Debug:**

```bash
# 1. Monitorar heap
jmap -heap <PID>

# 2. Dump heap para análise
jmap -dump:live,format=b,file=heap.bin <PID>
jhat heap.bin  # Analisa dump

# 3. Profiler (JProfiler, YourKit, etc)
```

---

## 6. PROBLEMAS DE AUTENTICAÇÃO/AUTORIZAÇÃO (Futuro)

### 6.1 Erro: "401 Unauthorized"

```java
// Quando implementar Spring Security:
// Verificar:
// 1. Token JWT válido
// 2. Expiração do token
// 3. Permissões (roles)
```

---

## 7. CHECKLIST DE DEBUG

Quando algo está quebrado:

- [ ] Verificar logs (console, arquivo)
- [ ] Reprochar o erro (passos para reproduzir)
- [ ] Testar endpoint com cURL/Postman
- [ ] Verificar BD (queries diretas em MySQL)
- [ ] Validar entrada (DTOs, @Valid)
- [ ] Checar variáveis de ambiente
- [ ] Verificar versões (Java, MySQL, libs)
- [ ] Limpar cache (mvn clean)
- [ ] Rebuild (mvn compile)
- [ ] Restart (Ctrl+C, mvn spring-boot:run)

---

## 8. FERRAMENTAS ÚTEIS

| Ferramenta | Uso |
|---|---|
| `curl` | Testar endpoints REST |
| `mysql` | Conectar ao banco |
| `jmap` | Analisar heap Java |
| `Postman` | GUI para testar APIs |
| `Git Bash` | Terminal no Windows |
| `DBeaver` | GUI MySQL |
| `VS Code REST Client` | Plugin para testar APIs |

---

**Última atualização**: 2026-06-12  
**Versão**: 1.0.0