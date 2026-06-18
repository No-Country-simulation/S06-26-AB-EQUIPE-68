# Especificação Técnica de Engenharia: App BiT (Ecossistema 360°)
## Stack Alvo: Java 21, Spring Boot 3.x, Spring AI, Thymeleaf, Tailwind CSS e MySQL

Este documento serve como uma diretiva de engenharia detalhada e contextualizada para guiar uma IA ou um desenvolvedor na construção completa do MVP do **App BiT**. O sistema deve ser autocontido, utilizando o ecossistema Spring para fornecer tanto a inteligência e APIs quanto a interface responsiva (Server-Side Rendering).

---

## 1. Arquitetura Geral do Sistema e Design de Camadas

Para garantir escalabilidade, legibilidade e fácil manutenção, o projeto adotará a arquitetura padrão em camadas do Spring Boot, centralizando a inteligência artificial através das abstrações nativas do **Spring AI**.

```text
[ Camada de Apresentação: Thymeleaf + Tailwind CSS ] (HTML5 Responsivo)
│
▼
[ Camada de Controle: @RestController & @Controller ] (Endpoints REST e Rotas de Telas)
│
▼
[ Camada de Negócio: @Service ] (Lógica de Match, Cálculo de Gap, Agentes de IA)
│
├──────────────────────────┐
▼                          ▼
[ Camada de Acesso a Dados: Spring Data JPA ] [ Integração IA: Spring AI ChatClient ]
│                          │
▼                          ▼
[ Banco de Dados: MySQL ]       [ Provedor de IA: LLM Externa (ex: Gemini/OpenAI) ]
```

---

## 2. Estrutura de Dependências (`pom.xml`)

O gerenciamento de dependências deve utilizar o **Spring AI BOM (Bill of Materials)** para garantir a compatibilidade das bibliotecas de IA, junto com os starters web, segurança, dados e validação.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.bitsystem</groupId>
    <artifactId>bitapp</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>bitapp</name>
    <description>App BiT - Ecossistema 360 de Orientação Pessoal e Profissional</description>

    <properties>
        <java.version>21</java.version>
        <spring-ai.version>1.0.0-M2</spring-ai.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## 3. Camada de Modelo de Dados (Entidades JPA)
Abaixo estão representadas as entidades fundamentais mapeadas em Java para suportar o armazenamento persistente no MySQL.

### 3.1. Entidade Usuário (`Usuario.java`)

```java
package com.bitsystem.bitapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "tb_usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dados Pessoais
    @NotBlank @Column(nullable = false)
    private String nome;

    @NotBlank @Email @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private String genero;
    private String escolaridade;
    private String continente;
    private String pais;
    private String estado;
    private String cidade;
    private String whatsapp;

    // Dados Profissionais
    private String nivelProfissional; // ex: Iniciante, Transição, Graduado sem Experiência
    private String areaTecnologia;    // ex: Java Back-End, Frontend, Análise de Dados
    private String objetivoPrincipal; // ex: Estudar, Buscar Emprego, Mudar de Emprego
    
    @Column(columnDefinition = "TEXT")
    private String competenciasAtuais; // Separadas por vírgula para processamento simples do MVP
}
```

### 3.2. Entidade Histórico de Saúde Mental (`HistoricoSaude.java`)

```java
package com.bitsystem.bitapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_historico_saude")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class HistoricoSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String humor; // Emoji ou texto representativo (ex: "Triste", "Ansioso")

    @Column(nullable = false)
    private Integer notaSemanal; // 1 a 5

    @Column(columnDefinition = "TEXT")
    private String contexto;

    @Column(nullable = false)
    private LocalDateTime dataRegistro = LocalDateTime.now();
    
    private Boolean derivouCvv;
}
```

## 4. DTOs de Request e Response (Contratos da API)
Definição estrita das estruturas de dados trafegadas nas requisições e respostas exigidas pelos endpoints principais.

### 4.1. DTO de Orientação (`OrientacaoDto.java`)

```java
package com.bitsystem.bitapp.dto;

import java.util.List;

public class OrientacaoDto {
    
    public record Request(
        Long usuarioId,
        String perfil,
        String nivel,
        String regiao,
        String idioma,
        Double lat,
        Double lng
    ) {}

    public record Response(
        Integer gapPercentual,
        List<String> gapItens,
        List<String> trilhaSugerida,
        List<String> vagasCompatibles,
        Double confianca
    ) {}
}
```

### 4.2. DTO de Saúde (`SaudeDto.java`)

```java
package com.bitsystem.bitapp.dto;

public class SaudeDto {

    public record Request(
        Long usuarioId,
        String humor,
        Integer notaSemanal,
        String contexto
    ) {}

    public record Response(
        String mensagem,
        String acaoSugerida,
        Boolean derivarCvv,
        Integer notaAtual,
        String alerta
    ) {}
}
```

## 5. Implementação da Camada de Inteligência com Spring AI
Abaixo está o coração do sistema, onde o `ChatClient` do Spring AI é utilizado com Engenharia de Prompts estruturada para retornar respostas diretamente mapeadas para os registros (Records) Java (Output Parsers).

### 5.1. Serviço de Orientação de Carreira (`OrientacaoService.java`)

```java
package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.OrientacaoDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class OrientacaoService {

    private final ChatClient chatClient;

    public OrientacaoService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                    Você é o Mentor de IA do App BiT, um ecossistema humanizado focado em apoiar minorias e grupos sub-representados na tecnologia.
                    Sua missão é cruzar os dados de perfil do usuário com as demandas do mercado tecnológico local/regional, avaliar gaps de competências,
                    e sugerir trilhas de estudo estruturadas (priorizando parcerias como Google Cloud GEAR e Oracle + Alura ONE).
                    Retorne SEMPRE a resposta estruturada estritamente de acordo com o formato JSON solicitado.
                    Seja realista, mas incentive fortemente o usuário para quebrar a síndrome de impostor.
                    """)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    public OrientacaoDto.Response processarOrientacao(OrientacaoDto.Request request) {
        String userPrompt = String.format("""
            Analise o seguinte perfil de usuário para fornecer vagas e trilhas:
            ID Usuário: %d
            Resumo Perfil: %s
            Nível Pretendido: %s
            Região: %s
            Idioma: %s
            Coordenadas do Usuário (Lat/Lng para checar vagas locais): %f, %f
            """,
            request.usuarioId(), request.perfil(), request.nivel(),
            request.regiao(), request.idioma(), request.lat(), request.lng());

        return this.chatClient.prompt()
                .user(userPrompt)
                .call()
                .entity(new ParameterizedTypeReference<OrientacaoDto.Response>() {});
    }
}
```

### 5.2. Serviço de Acolhimento e Saúde Mental (`SaudeMentalService.java`)

```java
package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.model.HistoricoSaude;
import com.bitsystem.bitapp.model.Usuario;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import com.bitsystem.bitapp.repository.UsuarioRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SaudeMentalService {

    private final ChatClient chatClient;
    private final HistoricoSaudeRepository saudeRepository;
    private final UsuarioRepository usuarioRepository;

    public SaudeMentalService(ChatClient.Builder chatClientBuilder,
                              HistoricoSaudeRepository saudeRepository,
                              UsuarioRepository usuarioRepository) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                    Você é o Agente de Acolhimento em Saúde Mental do App BiT, inspirado no modelo dos Alcoólicos Anônimos (escutar com empatia radical, validação cultural e sem julgamentos).
                    Você deve receber o estado de espírito (humor e contexto) do usuário, formular uma mensagem profundamente acolhedora e indicar uma ação prática humana e de baixo custo (ex: caminhar descalço na grama, escutar um podcast específico, ler um capítulo de livro).
                    A resposta precisa ser fornecida estritamente no formato JSON estruturado.
                    """)
                .build();
        this.saudeRepository = saudeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public SaudeDto.Response avaliarEstadoMental(SaudeDto.Request request) {
        // Validação de regra de negócio crítica: crise grave quando notaSemanal <= 3
        boolean derivarCvv = request.notaSemanal() <= 3;

        SaudeDto.Response response = this.chatClient.prompt()
                .user(String.format("""
                    Usuário ID: %d
                    Humor: %s
                    Nota Semanal: %d
                    Contexto: %s
                    """,
                    request.usuarioId(), request.humor(), request.notaSemanal(), request.contexto()))
                .call()
                .entity(new ParameterizedTypeReference<SaudeDto.Response>() {});

        Usuario usuario = usuarioRepository.findById(request.usuarioId()).orElse(null);
        HistoricoSaude historico = new HistoricoSaude();
        historico.setUsuario(usuario);
        historico.setHumor(request.humor());
        historico.setNotaSemanal(request.notaSemanal());
        historico.setContexto(request.contexto());
        historico.setDerivouCvv(derivarCvv);
        saudeRepository.save(historico);

        return response;
    }
}
```

## 6. Camada de Controladores REST (Endpoints Exigidos)
Controladores que expõem os endpoints do ecossistema para consumo ou integrações.

```java
package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.OrientacaoDto;
import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.service.OrientacaoService;
import com.bitsystem.bitapp.service.SaudeMentalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiApiController {

    private final OrientacaoService orientacaoService;
    private final SaudeMentalService saudeMentalService;

    public ApiApiController(OrientacaoService orientacaoService, SaudeMentalService saudeMentalService) {
        this.orientacaoService = orientacaoService;
        this.saudeMentalService = saudeMentalService;
    }

    @PostMapping("/orientar")
    public ResponseEntity<OrientacaoDto.Response> orientar(@RequestBody @Valid OrientacaoDto.Request request) {
        return ResponseEntity.ok(orientacaoService.processarOrientacao(request));
    }

    @PostMapping("/saude")
    public ResponseEntity<SaudeDto.Response> verificarSaude(@RequestBody @Valid SaudeDto.Request request) {
        return ResponseEntity.ok(saudeMentalService.avaliarEstadoMental(request));
    }
}
```

## 7. Frontend Integrado: Telas Dinâmicas (Thymeleaf + Tailwind CSS)
Para atender à exigência de **"Interface responsiva com ao menos home + uma tela funcional"** construída utilizando apenas o Spring Boot, utilizaremos controladores de visualização que renderizam páginas HTML acopladas com o framework CSS utility-first Tailwind.

### 7.1. Controlador de Telas (`WebController.java`)

```java
package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.model.Usuario;
import com.bitsystem.bitapp.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebController {

    private final UsuarioRepository usuarioRepository;

    public WebController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String exibirHome(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "home";
    }

    @PostMapping("/onboarding")
    public String processarOnboarding(@ModelAttribute Usuario usuario, Model model) {
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        model.addAttribute("usuario", usuarioSalvo);
        return "dashboard";
    }
}
```

### 7.2. Layout Base e Home Page (`src/main/resources/templates/home.html`)

```html
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>App BiT - Onboarding</title>
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@3.4.4/dist/tailwind.min.css" rel="stylesheet">
</head>
<body class="bg-slate-950 text-slate-100">
    <div class="min-h-screen flex flex-col items-center justify-center px-4 py-12">
        <div class="w-full max-w-4xl rounded-3xl bg-slate-900/90 border border-slate-700 shadow-2xl backdrop-blur-lg p-10">
            <div class="mb-10 text-center">
                <span class="text-3xl font-semibold text-cyan-300">BiT</span>
                <h1 class="mt-4 text-4xl font-bold text-white">App BiT</h1>
                <p class="mt-3 text-slate-400">Abordagem Humanizada 360°</p>
            </div>

            <div class="grid gap-8 lg:grid-cols-2">
                <div class="space-y-6">
                    <div class="rounded-3xl bg-slate-950/80 p-6 border border-slate-700">
                        <h2 class="text-xl font-semibold text-cyan-300">Você pertence ao mercado de tecnologia.</h2>
                        <p class="mt-4 text-slate-400">Não somos apenas vagas ou cursos. Somos um ecossistema projetado para apoiar sua empregabilidade, sua formação e o seu bem-estar mental.</p>
                    </div>
                    <div class="rounded-3xl bg-slate-950/80 p-6 border border-slate-700">
                        <h3 class="text-lg font-semibold text-white">🚀 Crie sua conta e inicie sua jornada</h3>
                        <form action="/onboarding" method="post" class="space-y-5 mt-6">
                            <div>
                                <label class="block mb-2 text-sm font-medium text-slate-300">Nome Completo</label>
                                <input type="text" name="nome" required class="w-full rounded-3xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500" />
                            </div>
                            <div>
                                <label class="block mb-2 text-sm font-medium text-slate-300">E-mail Corporativo/Pessoal</label>
                                <input type="email" name="email" required class="w-full rounded-3xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500" />
                            </div>
                            <div>
                                <label class="block mb-2 text-sm font-medium text-slate-300">Cidade</label>
                                <input type="text" name="cidade" class="w-full rounded-3xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500" />
                            </div>
                            <div>
                                <label class="block mb-2 text-sm font-medium text-slate-300">WhatsApp de Contato</label>
                                <input type="text" name="whatsapp" class="w-full rounded-3xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500" />
                            </div>

                            <div>
                                <label class="block mb-2 text-sm font-medium text-slate-300">Momento de Carreira / Nível</label>
                                <select name="nivelProfissional" class="w-full rounded-3xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500">
                                    <option>Estudante em Formação / Universitário</option>
                                    <option>Buscando Transição de Carreira</option>
                                    <option>Graduado sem experiência na área</option>
                                </select>
                            </div>
                            <div>
                                <label class="block mb-2 text-sm font-medium text-slate-300">Área Tecnológica Alvo</label>
                                <select name="areaTecnologia" class="w-full rounded-3xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500">
                                    <option>Java Back-End Development</option>
                                    <option>Web Development (Fullstack/Front)</option>
                                    <option>Data Analysis &amp; BI</option>
                                </select>
                            </div>
                            <div>
                                <label class="block mb-2 text-sm font-medium text-slate-300">Habilidades Atuais (separe por vírgula)</label>
                                <textarea name="competenciasAtuais" rows="3" class="w-full rounded-3xl border border-slate-700 bg-slate-900 px-4 py-3 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500"></textarea>
                            </div>

                            <button type="submit" class="w-full rounded-3xl bg-cyan-500 px-6 py-4 text-base font-semibold text-slate-950 transition hover:bg-cyan-400">Entrar no Ecossistema BiT</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

### 7.3. Tela do Painel Funcional (`src/main/resources/templates/dashboard.html`)

```html
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Painel do Aluno - App BiT</title>
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@3.4.4/dist/tailwind.min.css" rel="stylesheet">
</head>
<body class="bg-slate-950 text-slate-100">
    <div class="min-h-screen px-4 py-10 sm:px-6 lg:px-8">
        <div class="mx-auto max-w-6xl">
            <header class="mb-10 rounded-3xl border border-slate-800 bg-slate-900/90 p-8 shadow-2xl">
                <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                    <div>
                        <p class="text-cyan-300">BiT Dashboard</p>
                        <h1 class="mt-2 text-4xl font-bold text-white">Olá, Usuário</h1>
                    </div>
                </div>
            </header>

            <main class="grid gap-8 lg:grid-cols-2">
                <section class="rounded-3xl border border-slate-800 bg-slate-900/90 p-8 shadow-lg">
                    <h2 class="text-xl font-semibold text-cyan-300">🎯 Match de Oportunidades Reais</h2>
                    <p class="mt-4 text-5xl font-bold text-white">70%</p>
                    <div class="mt-6 rounded-3xl bg-slate-950/90 p-6 border border-slate-700">
                        <h3 class="text-lg font-semibold text-white">Desenvolvedor Java Júnior (Remoto)</h3>
                        <p class="mt-3 text-slate-400">O mercado já atende 70% das suas necessidades. Nós te damos suporte nos 30% restantes!</p>
                    </div>
                    <div class="mt-6">
                        <h4 class="text-base font-semibold text-white">O que falta para você conquistar a vaga:</h4>
                        <ul class="mt-4 space-y-3 text-slate-300">
                            <li class="rounded-3xl bg-slate-950/80 p-4 border border-slate-700">Spring Boot Avançado</li>
                            <li class="rounded-3xl bg-slate-950/80 p-4 border border-slate-700">Bancos de Dados Relacionais</li>
                        </ul>
                    </div>
                </section>

                <section class="rounded-3xl border border-slate-800 bg-slate-900/90 p-8 shadow-lg">
                    <h2 class="text-xl font-semibold text-cyan-300">📚 Trilha Concreta de Formação Sugerida</h2>
                    <div class="mt-6 space-y-5">
                        <article class="rounded-3xl bg-slate-950/90 p-6 border border-slate-700">
                            <h3 class="text-lg font-semibold text-white">Formação de Arquitetura Java (Alura + Oracle ONE)</h3>
                            <p class="mt-2 text-slate-400">Foco em fechar o gap de Spring Boot</p>
                            <span class="mt-4 inline-flex rounded-full bg-cyan-500 px-3 py-1 text-sm font-semibold text-slate-950">Gratuito</span>
                        </article>
                        <article class="rounded-3xl bg-slate-950/90 p-6 border border-slate-700">
                            <h3 class="text-lg font-semibold text-white">Google Cloud Engineers (Programa GEAR)</h3>
                            <p class="mt-2 text-slate-400">Infraestrutura em Nuvem para a vaga</p>
                            <span class="mt-4 inline-flex rounded-full bg-cyan-500 px-3 py-1 text-sm font-semibold text-slate-950">Gratuito</span>
                        </article>
                    </div>
                </section>
            </main>

            <section class="mt-8 rounded-3xl border border-slate-800 bg-slate-900/90 p-8 shadow-lg">
                <h2 class="text-xl font-semibold text-cyan-300">🌱 Check-in de Saúde Mental</h2>
                <p class="mt-3 text-slate-400">Sem julgamentos. Compartilhe seu estado para receber ações de acolhimento.</p>
                <div class="mt-6 grid gap-4 sm:grid-cols-5">
                    <button class="rounded-3xl bg-slate-950/80 px-4 py-5 text-2xl">😊</button>
                    <button class="rounded-3xl bg-slate-950/80 px-4 py-5 text-2xl">😴</button>
                    <button class="rounded-3xl bg-slate-950/80 px-4 py-5 text-2xl">😢</button>
                    <button class="rounded-3xl bg-slate-950/80 px-4 py-5 text-2xl">😰</button>
                    <button class="rounded-3xl bg-slate-950/80 px-4 py-5 text-2xl">🤯</button>
                </div>
                <form class="mt-6 space-y-4">
                    <textarea rows="4" placeholder="Compartilhe como se sente hoje..." class="w-full rounded-3xl border border-slate-700 bg-slate-950 px-4 py-4 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500"></textarea>
                    <button type="submit" class="rounded-3xl bg-cyan-500 px-6 py-4 text-base font-semibold text-slate-950">Enviar Registro Diário</button>
                </form>
            </section>
        </div>
    </div>
</body>
</html>
```

## 8. Arquivo de Instruções de Execução (`README.md`)
Este segmento detalha as instruções de inicialização do ecossistema e exemplos reais de requests.

```markdown
# App BiT - Guia de Execução do Sistema Local

## Pré-requisitos
- **Java 21 JDK** instalado e configurado no PATH
- **Maven 3.9+**
- Instância do **MySQL** ativa localmente
- Chave de API de LLM configurada como variável de ambiente (`OPENAI_API_KEY`)

## Configurações no `application.properties`
Crie ou altere as seguintes chaves em `src/main/resources/application.properties`:

```properties
spring.application.name=bitapp
spring.datasource.url=jdbc:mysql://localhost:3306/db_bitapp?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update

# Configuração do Agente Spring AI
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o
```

## Como Executar Localmente

1. Certifique-se de exportar sua chave de API no terminal:
```bash
export OPENAI_API_KEY="sua_chave_secreta_aqui"
```
2. Compile e empacote a aplicação utilizando o wrapper do Maven:
```bash
./mvnw clean package
```
3. Inicialize o servidor embutido do Spring Boot:
```bash
./mvnw spring-boot:run
```
4. Acesse a interface web do aplicativo via navegador em: **`http://localhost:8080/`**

## Exemplos de Cargas Úteis de Requisição e Resposta (Payloads API)

### Endpoint 1: Carreira e Orientação (`POST /api/orientar`)
**Exemplo de Request JSON:**

```json
{
  "usuarioId": 1,
  "perfil": "Estudante de Análise e Desenvolvimento de Sistemas com conhecimentos em lógica básica e HTML. Sente complexo de inferioridade por falta de mentorias.",
  "nivel": "Iniciante",
  "regiao": "Sudeste - SP",
  "idioma": "Português",
  "lat": -23.55052,
  "lng": -46.633308
}
```

**Exemplo de Response JSON (Gerado estruturado via Spring AI):**

```json
{
  "gapPercentual": 70,
  "gapItens": [
    "Programação Orientada a Objetos",
    "Conectores de Banco de Dados (JDBC/JPA)",
    "Fundamentos de Cloud Computing"
  ],
  "trilhaSugerida": [
    "Iniciar o Módulo 1 da trilha Java do Oracle + Alura ONE",
    "Inscrever-se no Programa GEAR do Google Cloud para conceitos de infraestrutura"
  ],
  "vagasCompatibles": [
    "Estágio de Engenharia de Software Back-End - SoftHouse SP",
    "Desenvolvedor Java Júnior Sub-representado (Remoto)"
  ],
  "confianca": 0.92
}
```

### Endpoint 2: Avaliação de Saúde Mental (`POST /api/saude`)
**Exemplo de Request JSON (Humor Baixo):**

```json
{
  "usuarioId": 1,
  "humor": "🤯 Sobrecarregado",
  "notaSemanal": 3,
  "contexto": "Muitas rejeições em processos seletivos e sensação persistente de que sempre falta algo e que não pertenço à área."
}
```

**Exemplo de Response JSON (Com ativação automática de desvio para o CVV):**

```json
{
  "mensagem": "Olá. Entendemos perfeitamente a sua dor e queremos que você saiba que o seu valor não é medido por respostas automatizadas de processos seletivos. Você pertence à tecnologia e estamos aqui para ouvir você sem qualquer julgamento.",
  "acaoSugerida": "Para acalmar a mente hoje, sugerimos que você faça uma pausa de 15 minutos, caminhe um pouco ao ar livre e assista a um episódio de uma série leve de comédia que você goste. Se sentir necessidade de conversar com alguém imediatamente, lembre-se do canal de apoio.",
  "derivarCvv": true,
  "notaAtual": 3,
  "alerta": "ALERTA CRÍTICO: Usuário necessita de apoio imediato."
}
```
```

---
Este arquivo Markdown fornece à IA todos os modelos lógicos, dependências estruturais e o comportamento de IA necessário para implementar a aplicação com fidelidade absoluta ao escopo especificado.
