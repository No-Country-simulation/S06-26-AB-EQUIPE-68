# 🏆 PADRÕES E MELHORES PRÁTICAS DO APP BiT

## 1. ARQUITETURA E DESIGN PATTERNS

### 1.1 Padrão MVC + REST

O projeto segue dois padrões simultaneamente:

**MVC (Model-View-Controller)**
```
WebController (GET /, POST /onboarding)
  ├─ Model: Usuario (entity JPA)
  └─ View: home.html, dashboard.html (Thymeleaf)
```

**REST (Representational State Transfer)**
```
ApiController (POST /api/orientar, POST /api/saude)
  ├─ Requests: DTOs (transfer objects)
  └─ Responses: JSON serializado
```

### 1.2 Injeção de Dependência (Dependency Injection)

```java
// ❌ Anti-pattern (acoplamento forte)
public class OrientacaoService {
  private GoogleGeminiClient client = new GoogleGeminiClient();
}

// ✅ Bom padrão (injeção via construtor)
@Service
public class OrientacaoService {
  private final GoogleGeminiClient geminiClient;
  
  public OrientacaoService(GoogleGeminiClient geminiClient) {
    this.geminiClient = geminiClient;  // Injetado
  }
}
```

**Benefícios:**
- Facilita testes (mock de dependências)
- Desacoplamento
- Reusabilidade
- Spring gerencia ciclo de vida

### 1.3 Repository Pattern (Data Access)

```java
// ❌ Anti-pattern (SQL direto)
public Usuario buscarUsuario(Long id) {
  return new Usuario(); // Hardcoded
}

// ✅ Bom padrão (abstração de persistência)
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByEmail(String email);
}

// Uso:
usuarioRepository.findById(1L)  // SELECT
usuarioRepository.save(usuario) // INSERT/UPDATE
usuarioRepository.findAll()     // SELECT *
```

**Benefícios:**
- SQL gerado automaticamente
- Query naming conventions
- Transações automáticas
- Testabilidade

---

## 2. VALIDAÇÕES E SEGURANÇA

### 2.1 Jakarta Validation (Bean Validation)

```java
// ❌ Anti-pattern (validação manual em controller)
@PostMapping("/usuario")
public void criarUsuario(String email) {
  if (email == null || email.isEmpty()) {
    throw new Exception("Email inválido");
  }
}

// ✅ Bom padrão (anotações declarativas)
public record UsuarioRequest(
  @NotBlank(message = "Email obrigatório")
  @Email(message = "Email inválido")
  String email
) {}

@PostMapping("/usuario")
public void criarUsuario(@Valid @RequestBody UsuarioRequest req) {
  // Spring valida automaticamente antes de chamar método
}
```

### 2.2 Valores Sensíveis (Secrets Management)

```properties
# ❌ NUNCA fazer commit
spring.gemini.api-key=AIzaSyD1234567890ABCDEF

# ✅ Usar variável de ambiente
spring.gemini.api-key=${GEMINI_API_KEY}
```

**Configuração segura:**
```bash
# .env (não commit)
GEMINI_API_KEY=AIzaSyD1234567890ABCDEF
MYSQL_PASSWORD=secure_pass_123

# Deploy via Docker Compose
docker-compose.yml (usa .env)

# Deploy em Kubernetes
kubectl create secret generic app-secrets \
  --from-literal=GEMINI_API_KEY=...
```

### 2.3 Detecção de Risco (Lógica de Negócio)

```java
// Derivação automática para CVV
boolean derivarCvv = request.notaSemanal() < 4;

if (derivarCvv) {
  // 1. Armazenar flag em BD
  historico.setDerivouCvv(true);
  saudeRepository.save(historico);
  
  // 2. Incluir alerta na resposta
  String alerta = "ALERTA CRÍTICO: Usuário necessita apoio imediato.";
  
  // 3. Em produção: disparar notificações
  // - Email para equipe de suporte
  // - WhatsApp bot
  // - CVV 188 hotline
}
```

---

## 3. TRATAMENTO DE ERROS

### 3.1 Exceções Customizadas

```java
// ❌ Anti-pattern (exceção genérica)
throw new Exception("Erro");

// ✅ Bom padrão (exceção específica com contexto)
public class UsuarioNaoEncontradoException extends RuntimeException {
  public UsuarioNaoEncontradoException(Long usuarioId) {
    super(String.format("Usuário com ID %d não encontrado", usuarioId));
  }
}

// Uso:
usuarioRepository.findById(id)
  .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
```

### 3.2 Global Exception Handler (Futuro)

```java
// ✅ Centralizar tratamento de erros
@ControllerAdvice
public class GlobalExceptionHandler {
  
  @ExceptionHandler(UsuarioNaoEncontradoException.class)
  public ResponseEntity<ErrorResponse> handleUsuarioNotFound(
    UsuarioNaoEncontradoException ex) {
    return ResponseEntity.status(404)
      .body(new ErrorResponse("USER_NOT_FOUND", ex.getMessage()));
  }
  
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleGeminiError(
    IllegalStateException ex) {
    return ResponseEntity.status(500)
      .body(new ErrorResponse("GEMINI_ERROR", "Erro ao processar IA"));
  }
}
```

---

## 4. PADRÕES DE CÓDIGO

### 4.1 Records vs Classes (Data Transfer Objects)

```java
// ❌ Anti-pattern (boilerplate manual)
public class OrientacaoRequest {
  private Long usuarioId;
  private String perfil;
  
  public Long getUsuarioId() { return usuarioId; }
  public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
  // ... 30 linhas mais
}

// ✅ Bom padrão (Records - Java 15+, imutável)
public record OrientacaoRequest(
  Long usuarioId,
  String perfil,
  String nivel,
  String regiao,
  String idioma,
  Double lat,
  Double lng
) {}

// Benefícios:
// - 1 linha vs 30 linhas
// - Imutável (thread-safe)
// - equals/hashCode/toString automático
// - Perfeito para DTOs
```

### 4.2 Lombok para Redução de Boilerplate

```java
// ❌ Anti-pattern (boilerplate manual em Entities)
@Entity
public class Usuario {
  private String nome;
  
  public String getNome() { return nome; }
  public void setNome(String nome) { this.nome = nome; }
  
  public Usuario() {}
  public Usuario(String nome) { this.nome = nome; }
  // ... equals, hashCode, toString
}

// ✅ Bom padrão (Lombok annotations)
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
  private String nome;
  // Lombok gera tudo automaticamente
}

// Uso:
Usuario user = Usuario.builder()
  .nome("Alice")
  .email("alice@example.com")
  .build();
```

### 4.3 Optional para Null Safety

```java
// ❌ Anti-pattern (NullPointerException risk)
Usuario usuario = usuarioRepository.findById(id).get();
String email = usuario.getEmail(); // Pode ser null

// ✅ Bom padrão (Null-safe)
usuarioRepository.findById(id)
  .ifPresent(usuario -> {
    System.out.println("Email: " + usuario.getEmail());
  })
  .ifPresentOrElse(
    usuario -> { /* existe */ },
    () -> { /* não existe */ }
  );

// Ou com orElseThrow:
Usuario usuario = usuarioRepository.findById(id)
  .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
```

---

## 5. PERFORMANCE E OTIMIZAÇÃO

### 5.1 Lazy Loading vs Eager Loading

```java
// ❌ Problema: N+1 queries
@OneToMany(fetch = FetchType.EAGER)
private List<HistoricoSaude> historico; // Carrega sempre!

// ✅ Solução: Lazy loading padrão
@OneToMany(fetch = FetchType.LAZY)
private List<HistoricoSaude> historico; // Carrega sob demanda

// Ou usar query customizada:
@Query("SELECT h FROM HistoricoSaude h WHERE h.usuario.id = ?1")
List<HistoricoSaude> findByUsuarioIdEagerly(Long usuarioId);
```

### 5.2 Caching (Futuro)

```java
// ✅ Cache com Spring Cache Abstraction
@Cacheable("orientacoes")
public OrientacaoDto.Response processarOrientacao(OrientacaoDto.Request req) {
  // Primeira vez: executa
  // Próximas: retorna do cache
  return orientacaoService.analisar(req);
}

@CacheEvict(value = "orientacoes", allEntries = true)
public void atualizarOrientacoes() {
  // Limpa cache
}
```

### 5.3 Connection Pooling (Automático)

```properties
# HikariCP (padrão do Spring Boot)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

---

## 6. LOGGING E MONITORAMENTO

### 6.1 SLF4J (Simple Logging Facade)

```java
// ❌ Anti-pattern (System.out.println)
System.out.println("Usuário criado: " + usuario);

// ✅ Bom padrão (SLF4J + Logback)
private static final Logger logger = LoggerFactory.getLogger(SaudeMentalService.class);

logger.debug("Iniciando avaliação de saúde para usuário {}", usuarioId);
logger.info("Derivação CVV ativada para usuário {}", usuarioId);
logger.warn("Taxa de erro alta em Gemini API");
logger.error("Falha ao salvar histórico de saúde", exception);
```

### 6.2 Structured Logging (Futuro)

```java
// ✅ JSON logging para stack traces
{
  "timestamp": "2026-06-12T10:30:00Z",
  "level": "ERROR",
  "logger": "SaudeMentalService",
  "message": "Falha ao parsear resposta Gemini",
  "usuarioId": 1,
  "exception": "com.fasterxml.jackson.core.JsonParseException",
  "traceback": "..."
}
```

---

## 7. TESTES (Futuro)

### 7.1 Testes Unitários (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class SaudeMentalServiceTest {
  
  @Mock
  GoogleGeminiClient geminiClientMock;
  
  @Mock
  HistoricoSaudeRepository saudeRepositoryMock;
  
  @InjectMocks
  SaudeMentalService service;
  
  @Test
  void testAvaliarEstadoMental_DerivaCVVQuandoNotaMenor4() {
    // Arrange
    SaudeDto.Request request = new SaudeDto.Request(1L, "Triste", 2, "Pressão");
    when(geminiClientMock.generateText(any())).thenReturn("""
      {"mensagem": "Entendo...", "acaoSugerida": "Respire"}
      """);
    
    // Act
    SaudeDto.Response response = service.avaliarEstadoMental(request);
    
    // Assert
    assertTrue(response.derivarCvv());
    assertEquals("ALERTA CRÍTICO...", response.alerta());
    verify(saudeRepositoryMock).save(any());
  }
}
```

### 7.2 Testes de Integração (TestRestTemplate)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiControllerIntegrationTest {
  
  @Autowired
  TestRestTemplate restTemplate;
  
  @Test
  void testOrientarEndpoint_Returns200() {
    OrientacaoDto.Request request = new OrientacaoDto.Request(
      1L, "Java, MySQL", "Iniciante", "São Paulo", "PT", -23.5, -46.6);
    
    ResponseEntity<OrientacaoDto.Response> response = restTemplate.postForEntity(
      "/api/orientar", request, OrientacaoDto.Response.class);
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody().gapPercentual());
  }
}
```

---

## 8. DOCUMENTAÇÃO

### 8.1 Javadoc (Comments Estruturados)

```java
/**
 * Processa uma requisição de orientação profissional.
 *
 * DETALHES:
 * 1. Constrói prompt estruturado para Gemini
 * 2. Chama API com contexto do usuário
 * 3. Parsea resposta JSON → Response DTO
 *
 * @param request OrientacaoDto.Request com perfil do usuário
 * @return OrientacaoDto.Response com análise de gaps e trilhas
 * @throws IllegalStateException se resposta Gemini inválida
 * @throws IllegalArgumentException se usuário não encontrado
 */
public OrientacaoDto.Response processarOrientacao(
  OrientacaoDto.Request request) {
  // ...
}
```

### 8.2 README.md (Project Setup)

```markdown
## QuickStart

### Prerequisites
- Java 21
- MySQL 8.0+
- Maven 3.8+

### Installation
1. Clone: `git clone ...`
2. Configure: `cp .env.example .env`
3. Set env: `export***REMOVED***
4. Build: `mvn clean install`
5. Run: `mvn spring-boot:run`

### API Endpoints
- POST `/api/orientar` - Análise de orientação
- POST `/api/saude` - Check-in de saúde mental
```

---

## 9. AUTOMAÇÃO E CI/CD (Futuro)

### 9.1 GitHub Actions

```yaml
name: Build & Test

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Build
        run: mvn clean package
      - name: Test
        run: mvn test
```

### 9.2 SonarQube (Code Quality)

```bash
# Análise de qualidade
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent verify \
  org.jacoco:jacoco-maven-plugin:report \
  sonar:sonar \
  -Dsonar.projectKey=bitapp \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=...
```

---

## 10. CHECKLIST DE QUALIDADE

### Antes de Commit

- [ ] Código compila sem erros
- [ ] Testes passam (100% coverage no ideal)
- [ ] Sem warnings (deprecation, unused imports)
- [ ] Seguindo padrões de naming (camelCase, PascalCase)
- [ ] Sem valores hardcoded (usar properties ou env vars)
- [ ] Sem exceções silenciosas (catch sem logar)
- [ ] Validações completadas
- [ ] Documentação/Javadoc atualizada

### Antes de Push

- [ ] Branch name descritivo (feat/user-auth, fix/gemini-parsing)
- [ ] Commit message claro (Use conventional commits)
- [ ] Sem merge conflicts
- [ ] CI/CD pipeline passou
- [ ] Code review aprovado (2 aprovadores)

### Antes de Deploy

- [ ] Testes de aceitação passaram
- [ ] Secrets injetados via env vars
- [ ] Migrations de BD testadas
- [ ] Performance benchmarked
- [ ] Logs estão corretos (sem PII)
- [ ] CHANGELOG atualizado

---

## 11. RECURSOS E REFERÊNCIAS

### Spring Framework
- [Spring Boot Reference](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Validation](https://www.baeldung.com/spring-boot-bean-validation)

### Java Best Practices
- [Effective Java - Joshua Bloch](https://www.oreilly.com/library/view/effective-java-3rd/9780134686288/)
- [Clean Code - Robert Martin](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)
- [Design Patterns - Gang of Four](https://en.wikipedia.org/wiki/Design_Patterns)

### Google Gemini API
- [Google AI Studio](https://makersuite.google.com/app/apikey)
- [Gemini API Docs](https://ai.google.dev/docs)
- [Prompt Engineering Guide](https://ai.google.dev/docs/guides/prompt_engineering)

### MySQL
- [MySQL Official Docs](https://dev.mysql.com/doc/)
- [Indexing Best Practices](https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html)

---

**Documento revisado**: 2026-06-12  
**Versão**: 1.0.0  
**Status**: MVP com comentários completos ✅