package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bitsystem.bitapp.dto.PontoLazerDto;
import com.bitsystem.bitapp.model.InfraestruturaRede;
import com.bitsystem.bitapp.repository.InfraestruturaRedeRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LazerServiceTest {

    @Mock
    private InfraestruturaRedeRepository infraestruturaRedeRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private Point pontoDe(double lat, double lng) {
        return geometryFactory.createPoint(new Coordinate(lng, lat));
    }

    private InfraestruturaRede antena(double lat, double lng, double densidade) {
        return InfraestruturaRede.builder()
                .posicao(pontoDe(lat, lng))
                .densidadePopulacional(densidade)
                .tipoTecnologia("4G")
                .build();
    }

    @Test
    void classificaZonaPelosPercentisDeDensidade() {
        // Uma antena colocada exatamente sobre cada um dos 3 pontos escolhidos,
        // com densidades bem distintas para forçar as 3 faixas.
        InfraestruturaRede antenaTranquila = antena(-27.609074, -48.454245, 10.0);  // sobre o ponto 1 (Lagoa)
        InfraestruturaRede antenaModerada = antena(-27.577496, -48.526197, 50.0);   // sobre o ponto 2 (Teatro)
        InfraestruturaRede antenaMovimentada = antena(-27.685926, -48.480379, 100.0); // sobre o ponto 8 (Praia Campeche)

        when(infraestruturaRedeRepository.findAll()).thenReturn(
                List.of(antenaTranquila, antenaModerada, antenaMovimentada));

        LazerService service = new LazerService(infraestruturaRedeRepository);
        List<PontoLazerDto.Response> pontos = service.listarPontos();

        Map<Integer, String> zonaPorId = pontos.stream()
                .collect(java.util.stream.Collectors.toMap(PontoLazerDto.Response::id, PontoLazerDto.Response::zonaMovimento));

        assertThat(zonaPorId.get(1)).isEqualTo("tranquila");
        assertThat(zonaPorId.get(2)).isEqualTo("moderada");
        assertThat(zonaPorId.get(8)).isEqualTo("movimentada");
    }

    @Test
    void listaTodosOs16PontosMesmoSemAntenas() {
        when(infraestruturaRedeRepository.findAll()).thenReturn(List.of());

        LazerService service = new LazerService(infraestruturaRedeRepository);
        List<PontoLazerDto.Response> pontos = service.listarPontos();

        assertThat(pontos).hasSize(16);
        assertThat(pontos).allMatch(p -> "moderada".equals(p.zonaMovimento()));
    }
}