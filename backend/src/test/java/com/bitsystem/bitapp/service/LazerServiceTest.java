package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bitsystem.bitapp.dto.PontoLazerDto;
import com.bitsystem.bitapp.model.InfraestruturaRede;
import com.bitsystem.bitapp.repository.InfraestruturaRedeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LazerServiceTest {

    @Mock
    private InfraestruturaRedeRepository infraestruturaRedeRepository;

    // Coordenadas dos 16 pontos de Lazer (LazerService.PONTOS, ids 1-16, na
    // mesma ordem) — copiadas aqui para poder colocar uma antena exatamente
    // sobre cada ponto, tornando os testes independentes de distâncias reais
    // entre pontos (distância zero sempre vence, então a antena mais próxima
    // de cada ponto é garantidamente a sua própria).
    private static final double[][] PONTOS_COORDS = {
        {-27.609074, -48.454245}, // 1
        {-27.577496, -48.526197}, // 2
        {-27.601751, -48.574500}, // 3
        {-27.602345, -48.523926}, // 4
        {-27.613403, -48.625779}, // 5
        {-27.595258, -48.552666}, // 6
        {-27.596914, -48.550084}, // 7
        {-27.685926, -48.480379}, // 8
        {-27.736035, -48.507903}, // 9
        {-27.597853, -48.521633}, // 10
        {-27.634363, -48.454295}, // 11
        {-27.597329, -48.553060}, // 12
        {-27.596584, -48.510198}, // 13
        {-27.429447, -48.396534}, // 14
        {-27.602035, -48.552115}, // 15
        {-27.726084, -48.507971}, // 16
    };

    private InfraestruturaRede antena(double lat, double lng, double densidade) {
        return InfraestruturaRede.builder()
                .latitude(lat)
                .longitude(lng)
                .densidadePopulacional(densidade)
                .tipoTecnologia("4G")
                .build();
    }

    /** Uma antena exatamente sobre cada um dos 16 pontos, densidade = id do ponto (1-16). */
    private List<InfraestruturaRede> antenasColocadasEmTodosOsPontos() {
        List<InfraestruturaRede> antenas = new ArrayList<>();
        for (int i = 0; i < PONTOS_COORDS.length; i++) {
            antenas.add(antena(PONTOS_COORDS[i][0], PONTOS_COORDS[i][1], i + 1));
        }
        return antenas;
    }

    private Map<Integer, String> zonaPorId(List<PontoLazerDto.Response> pontos) {
        return pontos.stream()
                .collect(java.util.stream.Collectors.toMap(PontoLazerDto.Response::id, PontoLazerDto.Response::zonaMovimento));
    }

    @Test
    void classificaZonaPelosPercentisDeDensidade() {
        // Densidades 1..16 (uma antena por ponto, distância zero) — percentis
        // 33/66 sobre um array ordenado de 16 valores (idxP33=5, idxP66=10,
        // 0-indexado) resultam em: 1-6 tranquila, 7-11 moderada, 12-16 movimentada.
        when(infraestruturaRedeRepository.findAll()).thenReturn(antenasColocadasEmTodosOsPontos());

        LazerService service = new LazerService(infraestruturaRedeRepository);
        Map<Integer, String> zonaPorId = zonaPorId(service.listarPontos());

        assertThat(zonaPorId.get(1)).isEqualTo("tranquila");   // densidade 1
        assertThat(zonaPorId.get(8)).isEqualTo("moderada");    // densidade 8
        assertThat(zonaPorId.get(16)).isEqualTo("movimentada"); // densidade 16
    }

    @Test
    void listaTodosOs16PontosMesmoSemAntenas() {
        when(infraestruturaRedeRepository.findAll()).thenReturn(List.of());

        LazerService service = new LazerService(infraestruturaRedeRepository);
        List<PontoLazerDto.Response> pontos = service.listarPontos();

        assertThat(pontos).hasSize(16);
        assertThat(pontos).allMatch(p -> "moderada".equals(p.zonaMovimento()));
    }

    // ── D2: percentis calculados só sobre as antenas mais próximas dos 16 pontos ──
    @Test
    void antenasNaoMaisProximasDeNenhumPontoNaoEntramNoCalculoDePercentil() {
        // Mesmas 16 antenas co-locadas (densidades 1..16) + duas antenas
        // "decoy" muito longe de todos os 16 pontos, com densidades extremas
        // (bem abaixo e bem acima de toda a faixa 1-16). Nunca serão a antena
        // mais próxima de nenhum ponto (distância zero sempre vence), mas sob
        // a regra ANTIGA (percentis sobre TODAS as antenas) entrariam no
        // array ordenado e deslocariam os cortes de percentil.
        List<InfraestruturaRede> antenas = new ArrayList<>(antenasColocadasEmTodosOsPontos());
        antenas.add(antena(0.0, 0.0, 0.0));         // decoy extremamente "tranquilo"
        antenas.add(antena(50.0, 50.0, 1_000_000.0)); // decoy extremamente "denso"
        when(infraestruturaRedeRepository.findAll()).thenReturn(antenas);

        LazerService service = new LazerService(infraestruturaRedeRepository);
        Map<Integer, String> zonaPorId = zonaPorId(service.listarPontos());

        // Mesmo resultado do teste sem decoys — prova que antenas nunca
        // "mais próximas" de nenhum ponto são excluídas do cálculo de percentil.
        assertThat(zonaPorId.get(1)).isEqualTo("tranquila");
        assertThat(zonaPorId.get(8)).isEqualTo("moderada");
        assertThat(zonaPorId.get(16)).isEqualTo("movimentada");
    }
}
