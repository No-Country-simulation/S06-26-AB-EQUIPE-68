package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.PontoLazerDto;
import com.bitsystem.bitapp.model.InfraestruturaRede;
import com.bitsystem.bitapp.repository.InfraestruturaRedeRepository;
import com.bitsystem.bitapp.util.GeoUtils;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 * SERVIÇO: LazerService
 * ============================================================================
 *
 * Pontos de Lazer & Cultura de Florianópolis (migrados de
 * frontend/js/lazer.js — antes 100% hardcoded no cliente, agora com o backend
 * como fonte única) enriquecidos com o selo de zona de movimento (Vísent-c):
 * para cada ponto, acha a antena Vísent mais próxima e classifica sua
 * densidade populacional em tranquila/moderada/movimentada pelos percentis
 * 33/66 das antenas Vísent mais próximas de cada um dos 16 pontos de Lazer
 * (podem repetir-se quando pontos compartilham a mesma antena mais próxima —
 * classificação relativa entre os pontos disponíveis, não contra o dataset
 * inteiro de antenas). O resultado é cacheado em memória — pontos e antenas
 * são estáticos, não há por que recalcular a cada request.
 *
 * @author BiT System
 * @version 1.0.0
 */
@Service
public class LazerService {

    private static final Logger log = LoggerFactory.getLogger(LazerService.class);

    private static final String ZONA_TRANQUILA = "tranquila";
    private static final String ZONA_MODERADA = "moderada";
    private static final String ZONA_MOVIMENTADA = "movimentada";

    // Migrados 1:1 de frontend/js/lazer.js (mesmos ids/nomes/coordenadas/descrições).
    private static final List<PontoLazerDto.Seed> PONTOS = List.of(
        new PontoLazerDto.Seed(1, "Lagoa da Conceição", "Parque", "LAGOA_CONCEICAO", -27.609074, -48.454245,
            "Cartão-postal de Floripa: lagoa cercada por morros, com bares à beira d'água, esportes náuticos (stand-up, caiaque) e a Avenida das Rendeiras.",
            true, true, "Diário 24h", List.of("natureza", "esporte", "gastronomia")),
        new PontoLazerDto.Seed(2, "Teatro Ademir Rosa (CIC)", "Teatro", "CBD_BEIRAMAR", -27.577496, -48.526197,
            "Principal casa de espetáculos de Florianópolis, no Centro Integrado de Cultura. Teatro, dança, música e ópera com programação diversificada.",
            false, true, "Seg–Sáb 10h–20h", List.of("cultura", "espetáculos", "teatro")),
        new PontoLazerDto.Seed(3, "Parque de Coqueiros", "Parque", "ESTREITO_CAPOEIRAS", -27.601751, -48.574500,
            "Parque urbano à beira-mar no continente, com pista de caminhada, ciclovia, academia ao ar livre e playground. Vista para a Baía Sul.",
            true, true, "Diário 6h–21h", List.of("natureza", "esporte", "família")),
        new PontoLazerDto.Seed(4, "MArquE – Museu de Arqueologia e Etnologia da UFSC", "Museu", "UFSC", -27.602345, -48.523926,
            "Museu da UFSC com acervo de sambaquis, cultura indígena e exposições temporárias. Entrada gratuita.",
            true, true, "Ter–Sex 9h–18h", List.of("cultura", "história", "educação")),
        new PontoLazerDto.Seed(5, "Mercado Público de São José", "Feira", "SAO_JOSE_CENTRO", -27.613403, -48.625779,
            "Mercado no centro histórico de São José, famoso pelas ostras frescas, gastronomia local e artesanato catarinense.",
            true, false, "Seg–Sex 7h–18h, Sáb 7h–14h", List.of("gastronomia", "regional", "compras")),
        new PontoLazerDto.Seed(6, "Biblioteca Pública de Santa Catarina", "Biblioteca", "CBD_BEIRAMAR", -27.595258, -48.552666,
            "Maior biblioteca pública do estado, no centro de Floripa. Amplo acervo, salas de estudo, wi-fi e programação cultural.",
            true, true, "Seg–Sex 8h–19h, Sáb 9h–13h", List.of("estudo", "leitura", "wifi")),
        new PontoLazerDto.Seed(7, "Museu Histórico de SC (Palácio Cruz e Sousa)", "Museu", "CBD_BEIRAMAR", -27.596914, -48.550084,
            "Museu na Praça XV, dentro do histórico Palácio Cruz e Sousa. Mobiliário de época, arte e a história de Santa Catarina.",
            true, false, "Ter–Sex 10h–18h, Sáb–Dom 10h–16h", List.of("cultura", "história", "arte")),
        new PontoLazerDto.Seed(8, "Praia do Campeche", "Praia", "CAMPECHE", -27.685926, -48.480379,
            "Uma das praias mais extensas do sul da ilha. Águas claras e fortes, ótima para surf e longas caminhadas na areia.",
            true, false, "Diário", List.of("praia", "surf", "natureza")),
        new PontoLazerDto.Seed(9, "Praia da Armação", "Praia", "CAMPECHE", -27.736035, -48.507903,
            "Antiga vila de pescadores no sul da ilha. Praia tranquila, igreja histórica e ponto de partida para a trilha da Lagoinha do Leste.",
            true, false, "Diário", List.of("praia", "história", "natureza")),
        new PontoLazerDto.Seed(10, "Teatro da UFSC", "Teatro", "UFSC", -27.597853, -48.521633,
            "Espetáculos ligados à graduação em Artes Cênicas. Teatro, dança e música com ingressos acessíveis.",
            false, true, "Conforme programação", List.of("teatro", "cultura", "estudantes")),
        new PontoLazerDto.Seed(11, "Praia da Joaquina", "Praia", "LAGOA_CONCEICAO", -27.634363, -48.454295,
            "Famosa pelo surf e pelas dunas de areia. Área de sandboard e trilha para o Morro da Lagoa.",
            true, false, "Diário", List.of("praia", "surf", "natureza")),
        new PontoLazerDto.Seed(12, "Mercado Público de Florianópolis", "Feira", "CBD_BEIRAMAR", -27.597329, -48.553060,
            "Mercado centenário com peixarias, boxes de café, artesanato e gastronomia local. Patrimônio histórico da cidade.",
            true, true, "Seg–Sáb 6h–18h", List.of("gastronomia", "história", "compras")),
        new PontoLazerDto.Seed(13, "Parque Ecológico do Córrego Grande", "Parque", "TRINDADE", -27.596584, -48.510198,
            "Parque urbano com trilhas, viveiro de mudas, orquidário, playground e fauna local. Ótimo para famílias e caminhadas.",
            true, true, "Ter–Dom 8h–18h", List.of("natureza", "família", "caminhada")),
        new PontoLazerDto.Seed(14, "Praia dos Ingleses", "Praia", "INGLESES", -27.429447, -48.396534,
            "Praia movimentada no norte da ilha, com boa infraestrutura, restaurantes e mar próprio para banho. Ideal para famílias.",
            true, false, "Diário", List.of("praia", "família", "gastronomia")),
        new PontoLazerDto.Seed(15, "CentroSul – Centro de Eventos", "Centro Cultural", "CBD_BEIRAMAR", -27.602035, -48.552115,
            "Maior centro de eventos de Floripa. Sedia feiras, shows, congressos e exposições durante todo o ano.",
            false, true, "Conforme eventos", List.of("shows", "eventos", "cultura")),
        new PontoLazerDto.Seed(16, "Parque da Lagoa do Peri", "Parque", "CAMPECHE", -27.726084, -48.507971,
            "Lagoa de água doce cercada por mata atlântica. Trilhas, banho na lagoa, observação de aves e área de piquenique.",
            true, true, "Diário 8h–18h", List.of("natureza", "trilhas", "aves"))
    );

    private final InfraestruturaRedeRepository infraestruturaRedeRepository;
    private final ConcurrentHashMap<Integer, String> zonaCache = new ConcurrentHashMap<>();
    private volatile double[] cortesPercentis; // [p33, p66], calculado uma vez

    public LazerService(InfraestruturaRedeRepository infraestruturaRedeRepository) {
        this.infraestruturaRedeRepository = infraestruturaRedeRepository;
    }

    public List<PontoLazerDto.Response> listarPontos() {
        List<InfraestruturaRede> antenas = carregarAntenas();

        // Resolve a antena mais próxima de CADA um dos 16 pontos primeiro —
        // os cortes de percentil (D2) são calculados sobre essas 16 densidades
        // (com repetição quando pontos compartilham a antena mais próxima),
        // não sobre o dataset inteiro de antenas.
        Map<Integer, Optional<InfraestruturaRede>> maisProximaPorPonto = PONTOS.stream()
            .collect(Collectors.toMap(PontoLazerDto.Seed::id, p -> antenaMaisProxima(p, antenas)));

        double[] cortes = obterCortesPercentis(maisProximaPorPonto.values());

        return PONTOS.stream()
            .map(p -> new PontoLazerDto.Response(
                p.id(), p.nome(), p.tipo(), p.regiao(), p.lat(), p.lng(), p.descricao(),
                p.gratuito(), p.acessivel(), p.horario(), p.tags(),
                zonaCache.computeIfAbsent(p.id(), id -> calcularZona(maisProximaPorPonto.get(p.id()), cortes))))
            .toList();
    }

    private List<InfraestruturaRede> carregarAntenas() {
        try {
            return infraestruturaRedeRepository.findAll();
        } catch (Exception ex) {
            log.warn("[LazerService] Banco indisponível ao buscar antenas, zona fica 'moderada': {}", ex.getMessage());
            return List.of();
        }
    }

    private Optional<InfraestruturaRede> antenaMaisProxima(PontoLazerDto.Seed ponto, List<InfraestruturaRede> antenas) {
        return antenas.stream()
            .filter(a -> a.getLatitude() != null && a.getLongitude() != null && a.getDensidadePopulacional() != null)
            .min(Comparator.comparingDouble(a -> distanciaAtePonto(ponto, a)));
    }

    private String calcularZona(Optional<InfraestruturaRede> maisProxima, double[] cortes) {
        if (maisProxima == null || maisProxima.isEmpty()) {
            return ZONA_MODERADA; // sem dado de antena disponível: classificação neutra
        }

        double densidade = maisProxima.get().getDensidadePopulacional();
        if (densidade <= cortes[0]) {
            return ZONA_TRANQUILA;
        }
        if (densidade <= cortes[1]) {
            return ZONA_MODERADA;
        }
        return ZONA_MOVIMENTADA;
    }

    private double distanciaAtePonto(PontoLazerDto.Seed ponto, InfraestruturaRede antena) {
        return GeoUtils.distanciaMetros(ponto.lat(), ponto.lng(), antena.getLatitude(), antena.getLongitude());
    }

    /**
     * Cortes de classificação = percentis 33 e 66 da densidade populacional
     * das antenas Vísent mais próximas de cada um dos 16 pontos de Lazer
     * (podem repetir-se quando pontos compartilham a mesma antena mais
     * próxima — classificação relativa entre os pontos, não contra todas as
     * antenas do dataset). Calculado uma única vez (pontos e antenas são
     * estáticos) e cacheado.
     */
    private double[] obterCortesPercentis(Collection<Optional<InfraestruturaRede>> maisProximasPorPonto) {
        double[] cache = cortesPercentis;
        if (cache != null) {
            return cache;
        }
        double[] densidades = maisProximasPorPonto.stream()
            .filter(Optional::isPresent)
            .map(o -> o.get().getDensidadePopulacional())
            .filter(java.util.Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .sorted()
            .toArray();

        double[] cortes;
        if (densidades.length == 0) {
            cortes = new double[] {0, 0};
        } else {
            int idxP33 = (int) Math.floor(densidades.length * 0.33);
            int idxP66 = (int) Math.floor(densidades.length * 0.66);
            cortes = new double[] {
                densidades[Math.min(idxP33, densidades.length - 1)],
                densidades[Math.min(idxP66, densidades.length - 1)]
            };
        }
        cortesPercentis = cortes;
        return cortes;
    }
}