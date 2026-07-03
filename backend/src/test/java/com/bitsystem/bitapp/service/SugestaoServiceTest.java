package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bitsystem.bitapp.dto.DicaLazerDto;
import com.bitsystem.bitapp.dto.SugestaoDto;
import com.bitsystem.bitapp.seed.DicasLazerSeed;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SugestaoServiceTest {

    @Mock private SaudeMentalService saudeMentalService;
    @Mock private com.bitsystem.bitapp.repository.UserRepository userRepository;
    @Mock private GeolocationService geolocationService;
    @Mock private LazerService lazerService;
    @Mock private com.bitsystem.bitapp.integration.GeminiClient geminiClient;
    @Mock private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private SugestaoService sugestaoService;

    private static final List<DicaLazerDto> DICAS_TRINDADE = DicasLazerSeed.DICAS_LAZER.get("TRINDADE");

    @Test
    void fallbackMapeiaHumorSobrecarregadoParaCalmaEPausa() {
        // TRINDADE só tem 2 dicas calma/pausa — o fallback completa até 3 com o
        // restante da região, mas as 2 da categoria certa devem sempre aparecer.
        SugestaoDto.Response resposta = sugestaoService.fallbackDeterministico("sobrecarregado", DICAS_TRINDADE, false);

        assertThat(resposta.sugestoes()).hasSize(3);
        List<String> titulosEsperados = DICAS_TRINDADE.stream()
            .filter(d -> d.categoria().equals("calma") || d.categoria().equals("pausa"))
            .map(DicaLazerDto::titulo)
            .toList();
        assertThat(resposta.sugestoes()).extracting(SugestaoDto.Item::titulo)
            .containsAll(titulosEsperados);
    }

    @Test
    void fallbackMapeiaHumorFelizParaSocialECultura() {
        SugestaoDto.Response resposta = sugestaoService.fallbackDeterministico("feliz", DICAS_TRINDADE, false);

        List<String> titulosEsperados = DICAS_TRINDADE.stream()
            .filter(d -> d.categoria().equals("social") || d.categoria().equals("cultura"))
            .map(DicaLazerDto::titulo)
            .toList();
        assertThat(resposta.sugestoes()).extracting(SugestaoDto.Item::titulo)
            .containsAll(titulosEsperados);
    }

    @Test
    void fallbackPriorizaOfflineFriendlyQuandoConectividadeFraca() {
        // DICAS_GERAIS tem 2 itens offlineFriendly=false ("calma" e "social") — usando
        // humor "ansioso" (categorias calma/natureza), a dica de música (calma,
        // offline=false) deve perder posição para as offlineFriendly=true.
        SugestaoDto.Response semOffline = sugestaoService.fallbackDeterministico(
                "ansioso", DicasLazerSeed.DICAS_GERAIS, false);
        SugestaoDto.Response comOffline = sugestaoService.fallbackDeterministico(
                "ansioso", DicasLazerSeed.DICAS_GERAIS, true);

        assertThat(comOffline.sugestoes()).extracting(SugestaoDto.Item::titulo)
            .doesNotContain("Ouça música que te acalma");
        assertThat(semOffline.sugestoes()).isNotEmpty();
    }
}