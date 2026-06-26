package com.bitsystem.bitapp.service.impl;

import com.bitsystem.bitapp.dto.CourseRecommendationDto;
import com.bitsystem.bitapp.dto.NetworkStatusDto;
import com.bitsystem.bitapp.domain.User;
import com.bitsystem.bitapp.model.InfraestruturaRede;
import com.bitsystem.bitapp.repository.InfraestruturaRedeRepository;
import com.bitsystem.bitapp.repository.UserRepository;
import com.bitsystem.bitapp.service.GeolocationService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 * SERVIÇO: GeolocationServiceImpl
 * ============================================================================
 *
 * Implementação do serviço de geolocalização para avaliar o status da rede
 * com base na localização do usuário e torres de infraestrutura próximas.
 *
 * @author BiT System
 * @version 1.0.0
 */
@Service
public class GeolocationServiceImpl implements GeolocationService {

    private static final Logger log = LoggerFactory.getLogger(GeolocationServiceImpl.class);

    private final UserRepository userRepository;
    private final InfraestruturaRedeRepository infraestruturaRedeRepository;
    private final GeometryFactory geometryFactory;

    public GeolocationServiceImpl(
        UserRepository userRepository,
        InfraestruturaRedeRepository infraestruturaRedeRepository
    ) {
        this.userRepository = userRepository;
        this.infraestruturaRedeRepository = infraestruturaRedeRepository;
        this.geometryFactory = new GeometryFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NetworkStatusDto getNetworkStatus(
        Long usuarioId,
        double raioMetros
    ) {
        Optional<User> userOptional;
        try {
            userOptional = userRepository.findById(usuarioId);
        } catch (Exception ex) {
            log.warn("[GeolocationService] Banco indisponível, retornando status genérico: {}", ex.getMessage());
            return buildFallbackNetworkStatus();
        }

        if (userOptional.isEmpty()) {
            return NetworkStatusDto.builder()
                .status("Usuário Não Encontrado")
                .tecnologiaPredominante("N/A")
                .cssClass("text-gray-500")
                .build();
        }

        User user = userOptional.get();
        Point pontoAluno = user.getLocalizacao();

        if (pontoAluno == null) {
            return NetworkStatusDto.builder()
                .status("Localização do Usuário Não Definida")
                .tecnologiaPredominante("N/A")
                .cssClass("text-gray-500")
                .build();
        }

        List<InfraestruturaRede> allTorres;
        try {
            allTorres = infraestruturaRedeRepository.findAll();
        } catch (Exception ex) {
            log.warn("[GeolocationService] Banco indisponível ao buscar torres: {}", ex.getMessage());
            return buildFallbackNetworkStatus();
        }
        List<InfraestruturaRede> torresProximas = new java.util.ArrayList<>();
        for (InfraestruturaRede t : allTorres) {
            Point pos = t.getPosicao();
            if (pos != null) {
                double dist = calcularDistanciaMetros(
                    pontoAluno.getY(), pontoAluno.getX(),
                    pos.getY(), pos.getX()
                );
                if (dist <= raioMetros) {
                    torresProximas.add(t);
                }
            }
        }

        if (torresProximas.isEmpty()) {
            return NetworkStatusDto.builder()
                .status("Instável")
                .tecnologiaPredominante("Nenhuma torre encontrada")
                .cssClass("text-yellow-500")
                .build();
        } else {
            // Priorizar 5G, depois 4G, depois 3G
            Optional<InfraestruturaRede> melhorTecnologia = torresProximas
                .stream()
                .sorted(
                    Comparator.comparing(t -> {
                        if (
                            "5G".equalsIgnoreCase(t.getTipoTecnologia())
                        ) return 0;
                        if (
                            "4G".equalsIgnoreCase(t.getTipoTecnologia())
                        ) return 1;
                        if (
                            "3G".equalsIgnoreCase(t.getTipoTecnologia())
                        ) return 2;
                        return 3; // Outras tecnologias (inferior)
                    })
                )
                .findFirst();

            String status = "Estável";
            String tecnologia = melhorTecnologia
                .map(InfraestruturaRede::getTipoTecnologia)
                .orElse("Desconhecida");
            String cssClass = "text-blue-500";
            boolean sugerirOffline = false;
            String recomendacao =
                "Conexão excelente. Aproveite todos os recursos online.";

            if (tecnologia.contains("3G")) {
                status = "Instável";
                cssClass = "text-yellow-500";
                sugerirOffline = true;
                recomendacao =
                    "Conexão limitada. Recomendamos baixar conteúdos para estudo offline.";
            }

            List<CourseRecommendationDto> recommendations = gerarRecomendacoes(
                user,
                sugerirOffline
            );

            return NetworkStatusDto.builder()
                .status(status)
                .tecnologiaPredominante(tecnologia)
                .cssClass(cssClass)
                .sugerirOffline(sugerirOffline)
                .recomendacao(recomendacao)
                .courseRecommendations(recommendations)
                .build();
        }
    }

    /**
     * Gera 3 recomendações de cursos baseadas no perfil do usuário e status da rede.
     */
    private List<CourseRecommendationDto> gerarRecomendacoes(
        User user,
        boolean offline
    ) {
        List<CourseRecommendationDto> list = new ArrayList<>();
        String area =
            user.getAreaTecnologia() != null
                ? user.getAreaTecnologia()
                : "Java";
        String nivel =
            user.getNivelProfissional() != null
                ? user.getNivelProfissional()
                : "Estudante";

        if ("Java".equalsIgnoreCase(area)) {
            list.add(
                CourseRecommendationDto.builder()
                    .title("Java Fundamentals: Core Syntax")
                    .description(
                        offline
                            ? "Versão otimizada para download."
                            : "Início da jornada Back-end."
                    )
                    .targetCareerLevel(nivel)
                    .status("Disponível")
                    .build()
            );
            list.add(
                CourseRecommendationDto.builder()
                    .title("Spring Boot & REST APIs")
                    .description("Construa serviços escaláveis.")
                    .targetCareerLevel(nivel)
                    .status("Recomendado")
                    .build()
            );
            list.add(
                CourseRecommendationDto.builder()
                    .title("Hibernate & JPA Persistence")
                    .description("Domine a camada de dados.")
                    .targetCareerLevel(nivel)
                    .status("Essencial")
                    .build()
            );
        } else if ("Web".equalsIgnoreCase(area)) {
            list.add(
                CourseRecommendationDto.builder()
                    .title("Modern JavaScript (ES6+)")
                    .description(
                        offline
                            ? "Material em PDF e vídeos leves."
                            : "Base sólida para frameworks."
                    )
                    .targetCareerLevel(nivel)
                    .status("Disponível")
                    .build()
            );
            list.add(
                CourseRecommendationDto.builder()
                    .title("React.js: Hooks e Context API")
                    .description("Crie interfaces reativas.")
                    .targetCareerLevel(nivel)
                    .status("Recomendado")
                    .build()
            );
            list.add(
                CourseRecommendationDto.builder()
                    .title("Tailwind CSS Essentials")
                    .description("Estilização moderna e rápida.")
                    .targetCareerLevel(nivel)
                    .status("Próximo Passo")
                    .build()
            );
        } else {
            list.add(
                CourseRecommendationDto.builder()
                    .title("SQL for Data Science")
                    .description(
                        offline
                            ? "Exercícios offline inclusos."
                            : "Lógica de dados e queries."
                    )
                    .targetCareerLevel(nivel)
                    .status("Disponível")
                    .build()
            );
            list.add(
                CourseRecommendationDto.builder()
                    .title("Python for Data Analysis")
                    .description("Manipulação com Pandas e Numpy.")
                    .targetCareerLevel(nivel)
                    .status("Recomendado")
                    .build()
            );
            list.add(
                CourseRecommendationDto.builder()
                    .title("Business Intelligence com Power BI")
                    .description("Visualização de dados estratégica.")
                    .targetCareerLevel(nivel)
                    .status("Avançado")
                    .build()
            );
        }

        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point createPoint(double latitude, double longitude) {
        // SRID 4326 é para coordenadas geográficas (latitude/longitude)
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    /**
     * Resposta fallback quando o banco de dados está indisponível.
     */
    private NetworkStatusDto buildFallbackNetworkStatus() {
        return NetworkStatusDto.builder()
            .status("Instável")
            .tecnologiaPredominante("Desconhecida (modo offline)")
            .cssClass("text-yellow-500")
            .sugerirOffline(true)
            .recomendacao("Conexão indisponível no momento. Recomendamos baixar conteúdos para estudo offline.")
            .courseRecommendations(List.of(
                CourseRecommendationDto.builder()
                    .title("Estude offline: Material disponível para download")
                    .description("BaixePDFs e vídeos leves para continuar estudando sem conexão.")
                    .targetCareerLevel("Todos")
                    .status("Disponível")
                    .build(),
                CourseRecommendationDto.builder()
                    .title("Pratique com exercícios offline")
                    .description("Resolva desafios de código que não precisam de internet.")
                    .targetCareerLevel("Júnior")
                    .status("Recomendado")
                    .build(),
                CourseRecommendationDto.builder()
                    .title("Organize seus estudos com Kanban")
                    .description("Planeje sua semana de estudos com um quadro físico ou digital.")
                    .targetCareerLevel("Todos")
                    .status("Dica")
                    .build()
            ))
            .build();
    }

    private double calcularDistanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Raio da Terra em metros
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
