package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.User;
import com.bitsystem.bitapp.dto.SaudeDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.model.HistoricoSaude;
import com.bitsystem.bitapp.repository.HistoricoSaudeRepository;
import com.bitsystem.bitapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SaudeMentalService {

    private static final Logger log = LoggerFactory.getLogger(SaudeMentalService.class);

    private final HistoricoSaudeRepository saudeRepository;
    private final UserRepository userRepository;
    private final EmotionResponseProvider emotionResponseProvider;
    private final FallbackStorage fallbackStorage;

    public SaudeMentalService(
            HistoricoSaudeRepository saudeRepository,
            UserRepository userRepository,
            EmotionResponseProvider emotionResponseProvider,
            FallbackStorage fallbackStorage) {
        this.saudeRepository = saudeRepository;
        this.userRepository = userRepository;
        this.emotionResponseProvider = emotionResponseProvider;
        this.fallbackStorage = fallbackStorage;
    }

    public SaudeDto.Response avaliarEstadoMental(SaudeDto.Request request) {
        boolean derivarCvv = request.notaSemanal() < 4;

        // ── EmotionResponseProvider é 100% local, sempre funciona ──────────
        SaudeDto.RawResponse rawResponse = emotionResponseProvider.resolve(request.humor(), request.notaSemanal());

        String alerta = derivarCvv
                ? "ALERTA_CRITICO: Indicadores de sofrimento severo ou humor debilitado. Direcionando canais de apoio imediato."
                : "ESTÁVEL";

        SaudeDto.Response response = new SaudeDto.Response(
                rawResponse.mensagem(),
                rawResponse.acaoSugerida(),
                derivarCvv,
                request.notaSemanal(),
                alerta);

        // ── Tentar salvar no banco ─────────────────────────────────────────
        try {
            User user = userRepository
                    .findById(request.usuarioId())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                            "Utilizador não encontrado para o ID informado: " + request.usuarioId()));

            HistoricoSaude historico = HistoricoSaude.builder()
                    .user(user)
                    .humor(request.humor())
                    .notaSemanal(request.notaSemanal())
                    .contexto(request.contexto())
                    .derivouCvv(derivarCvv)
                    .build();
            saudeRepository.save(historico);

            log.info("[SaudeMentalService] Histórico salvo no banco: usuarioId={}", request.usuarioId());

        } catch (BusinessException ex) {
            throw ex;

        } catch (Exception ex) {
            // ── Fallback: salvar em memória ────────────────────────────────
            log.warn("[SaudeMentalService] Banco indisponível, salvando em memória: {}", ex.getMessage());
            fallbackStorage.saveSaudeRecord(
                request.usuarioId(), request.humor(), request.notaSemanal(),
                request.contexto(), derivarCvv
            );
        }

        return response;
    }
}
