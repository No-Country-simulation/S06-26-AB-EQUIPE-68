package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.OrientacaoDto;
import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.dto.NetworkStatusDto;
import com.bitsystem.bitapp.dto.UsuarioDto;
import com.bitsystem.bitapp.model.Usuario;
import com.bitsystem.bitapp.repository.UsuarioRepository;
import com.bitsystem.bitapp.service.GeolocationService;
import com.bitsystem.bitapp.service.OrientacaoService;
import com.bitsystem.bitapp.service.SaudeMentalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================================
 * CONTROLLER REST: ApiController
 * ============================================================================
 *
 * Expõe todos os endpoints REST para o frontend estático desacoplado.
 * O backend é agora 100% API REST — sem Thymeleaf ou renderização server-side.
 *
 * ENDPOINTS:
 * ┌─ POST /api/usuarios        → Cadastro/atualização de usuário (onboarding)
 * ├─ POST /api/orientar        → Análise de orientação profissional
 * ├─ POST /api/saude           → Check-in de saúde mental
 * └─ GET  /api/network-status  → Status de conectividade de rede
 *
 * CORS configurado globalmente em CorsConfig.java.
 *
 * @author BiT System
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final OrientacaoService orientacaoService;
    private final SaudeMentalService saudeMentalService;
    private final GeolocationService geolocationService;
    private final UsuarioRepository usuarioRepository;

    public ApiController(
            OrientacaoService orientacaoService,
            SaudeMentalService saudeMentalService,
            GeolocationService geolocationService,
            UsuarioRepository usuarioRepository) {
        this.orientacaoService = orientacaoService;
        this.saudeMentalService = saudeMentalService;
        this.geolocationService = geolocationService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * ========== ENDPOINT 0: CADASTRO / ATUALIZAÇÃO DE USUÁRIO ==========
     *
     * POST /api/usuarios
     *
     * Substitui o antigo POST /onboarding do Thymeleaf.
     * Implementa "upsert": se o email já existir, atualiza; caso contrário, cria.
     * O frontend armazena a resposta no localStorage para uso nas demais chamadas.
     *
     * @param request UsuarioDto.Request com dados do formulário de onboarding
     * @return UsuarioDto.Response com os dados do usuário salvo (HTTP 201 ou 200)
     */
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDto.Response> cadastrarUsuario(
            @RequestBody @Valid UsuarioDto.Request request) {

        Usuario usuarioExistente = usuarioRepository
                .findByEmail(request.email())
                .orElse(null);

        Usuario usuario = (usuarioExistente != null) ? usuarioExistente : new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setCidade(request.cidade());
        usuario.setWhatsapp(request.whatsapp());
        usuario.setNivelProfissional(request.nivelProfissional());
        usuario.setAreaTecnologia(request.areaTecnologia());
        usuario.setCompetenciasAtuais(request.competenciasAtuais());

        Usuario salvo = usuarioRepository.save(usuario);

        UsuarioDto.Response response = new UsuarioDto.Response(
                salvo.getId(),
                salvo.getNome(),
                salvo.getEmail(),
                salvo.getCidade(),
                salvo.getWhatsapp(),
                salvo.getNivelProfissional(),
                salvo.getAreaTecnologia(),
                salvo.getCompetenciasAtuais()
        );

        HttpStatus status = (usuarioExistente == null) ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * ========== ENDPOINT 1: ORIENTAÇÃO PROFISSIONAL ==========
     *
     * POST /api/orientar
     *
     * Processa requisição de análise de orientação profissional/acadêmica.
     * Envia perfil do usuário a Gemini para recomendações personalizadas.
     *
     * FLUXO:
     * 1. Client envia POST com OrientacaoDto.Request (JSON)
     * 2. Spring deserializa JSON → OrientacaoDto.Request
     * 3. @Valid valida campos (usuarioId, perfil, nivel obrigatórios)
     * 4. OrientacaoService.processarOrientacao() é invocado
     * 5. Service chama Gemini, parsea resposta JSON
     * 6. Response é serializada para JSON + HTTP 200
     *
     * REQUISIÇÃO (example):
     * {
     *   "usuarioId": 1,
     *   "perfil": "Java, MySQL, Git",
     *   "nivel": "Iniciante",
     *   "regiao": "São Paulo, Brasil",
     *   "idioma": "Português",
     *   "lat": -23.5505,
     *   "lng": -46.6333
     * }
     *
     * RESPOSTA (example):
     * {
     *   "gapPercentual": 35,
     *   "gapItens": ["Spring Boot", "REST APIs", "Docker"],
     *   "trilhaSugerida": ["Curso Spring Boot", "Projeto prático"],
     *   "vagasCompatibles": ["Java Dev Jr", "Backend Dev"],
     *   "confianca": 0.85
     * }
     *
     * STATUS HTTP:
     * - 200 OK: Análise realizada com sucesso
     * - 400 Bad Request: Validação falhou (campos obrigatórios)
     * - 404 Not Found: Usuário não encontrado (service)
     * - 500 Internal Server Error: Erro Gemini ou parsing
     *
     * @param request OrientacaoDto.Request com dados do usuário
     * @return ResponseEntity com OrientacaoDto.Response (HTTP 200)
     */
    @PostMapping("/orientar")
    public ResponseEntity<OrientacaoDto.Response> orientar(@RequestBody @Valid OrientacaoDto.Request request) {
        return ResponseEntity.ok(orientacaoService.processarOrientacao(request));
    }

    /**
     * ========== ENDPOINT 2: CHECK-IN DE SAÚDE MENTAL ==========
     *
     * POST /api/saude
     *
     * Processa check-in de saúde mental com acolhimento empático via IA.
     * Armazena histórico e detecta alertas críticos automaticamente.
     *
     * FLUXO:
     * 1. Client envia POST com SaudeDto.Request (JSON)
     * 2. Spring deserializa JSON → SaudeDto.Request
     * 3. @Valid valida campos (usuarioId, humor, nota obrigatórios)
     * 4. SaudeMentalService.avaliarEstadoMental() é invocado
     * 5. Service detecta risco (nota < 4), chama Gemini, salva no BD
     * 6. Response com alerta crítico (se aplicável)
     * 7. Resposta serializada para JSON + HTTP 200
     *
     * REQUISIÇÃO (example):
     * {
     *   "usuarioId": 1,
     *   "humor": "😰 Ansioso",
     *   "notaSemanal": 2,
     *   "contexto": "Pressão do deadline do projeto"
     * }
     *
     * RESPOSTA (example):
     * {
     *   "mensagem": "Entendo sua ansiedade... Você é capaz...",
     *   "acaoSugerida": "Faça uma caminhada de 10 minutos",
     *   "derivarCvv": true,
     *   "notaAtual": 2,
     *   "alerta": "ALERTA CRÍTICO: Usuário necessita de apoio imediato."
     * }
     *
     * DETECÇÃO DE RISCO:
     * - Nota 1-3 → derivarCvv = true (alerta crítico)
     * - Nota 4-5 → derivarCvv = false (situação controlada)
     * - Histórico é persistido para análise de tendências
     *
     * STATUS HTTP:
     * - 200 OK: Check-in processado e armazenado
     * - 400 Bad Request: Validação falhou
     * - 404 Not Found: Usuário não encontrado
     * - 500 Internal Server Error: Erro Gemini ou BD
     *
     * @param request SaudeDto.Request com humor e contexto
     * @return ResponseEntity com SaudeDto.Response (HTTP 200)
     */
    @PostMapping("/saude")
    public ResponseEntity<SaudeDto.Response> verificarSaude(@RequestBody @Valid SaudeDto.Request request) {
        return ResponseEntity.ok(saudeMentalService.avaliarEstadoMental(request));
    }

    /**
     * ========== ENDPOINT 3: STATUS DE CONECTIVIDADE DE REDE ==========
     *
     * GET /api/network-status/{usuarioId}
     *
     * Retorna o status de conectividade da rede para um usuário, com base na sua localização.
     * Calcula se há torres 5G próximas (5km para estável) ou 4G/3G (5km para instável).
     *
     * REQUISIÇÃO:
     * GET /api/network-status/1?raioMetros=5000
     *
     * RESPOSTA (example para estável):
     * {
     *   "status": "Estável",
     *   "tecnologiaPredominante": "5G",
     *   "cssClass": "text-blue-500"
     * }
     *
     * RESPOSTA (example para instável):
     * {
     *   "status": "Instável",
     *   "tecnologiaPredominante": "3G",
     *   "cssClass": "text-yellow-500"
     * }
     *
     * @param usuarioId O ID do usuário para verificar o status da rede.
     * @param raioMetros O raio em metros para considerar a proximidade de torres.
     * @return ResponseEntity com NetworkStatusDto (HTTP 200).
     */
    @GetMapping("/network-status/{usuarioId}")
    public ResponseEntity<NetworkStatusDto> getNetworkStatus(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "5000") double raioMetros) {
        return ResponseEntity.ok(geolocationService.getNetworkStatus(usuarioId, raioMetros));
    }
}
