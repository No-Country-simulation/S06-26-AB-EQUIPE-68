package com.bitsystem.bitapp.service.impl;

import com.bitsystem.bitapp.model.InfraestruturaRede;
import com.bitsystem.bitapp.repository.InfraestruturaRedeRepository;
import com.bitsystem.bitapp.service.GeolocationService;
import com.bitsystem.bitapp.service.VisentDataIngestionService;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================================
 * IMPLEMENTAÇÃO: VisentDataIngestionServiceImpl
 * ============================================================================
 *
 * Processa a importação dos dados do Dataset Vísent. Resolve os cabeçalhos
 * de forma dinâmica e converte dados tabulares em entidades georreferenciadas.
 *
 * @author BiT System
 * @version 1.0.0
 */
@Service
public class VisentDataIngestionServiceImpl
    implements VisentDataIngestionService
{

    private static final Logger log = LoggerFactory.getLogger(
        VisentDataIngestionServiceImpl.class
    );

    private final InfraestruturaRedeRepository repository;
    private final GeolocationService geolocationService;

    public VisentDataIngestionServiceImpl(
        InfraestruturaRedeRepository repository,
        GeolocationService geolocationService
    ) {
        this.repository = repository;
        this.geolocationService = geolocationService;
    }

    @Override
    @Transactional
    public void importarDataset(
        String antenasCsvPath,
        String concentracaoCsvPath
    ) {
        try {
            log.info("Iniciando carga de dados do Dataset Vísent...");

            // 1. Carregar antenas
            Map<String, TempAntenna> antenasMap = lerAntenas(antenasCsvPath);
            log.info(
                "{} antenas pré-carregadas do arquivo {}",
                antenasMap.size(),
                antenasCsvPath
            );

            // 2. Acoplar dados de concentração/tráfego se o arquivo existir
            if (concentracaoCsvPath != null && !concentracaoCsvPath.isEmpty()) {
                vincularConcentracao(antenasMap, concentracaoCsvPath);
            }

            // 3. Converter para Entidades JPA com Pontos Geográficos
            List<InfraestruturaRede> entidadesParaSalvar = new ArrayList<>();
            for (TempAntenna temp : antenasMap.values()) {
                if (temp.latitude == null || temp.longitude == null) {
                    continue; // Pular registros sem coordenadas válidas
                }

                Point posicaoGeo = geolocationService.createPoint(
                    temp.latitude,
                    temp.longitude
                );

                InfraestruturaRede infra = InfraestruturaRede.builder()
                    .codigoEstacao(temp.ecgi)
                    .operadora(
                        temp.operadora != null ? temp.operadora : "Claro"
                    )
                    .tipoTecnologia(
                        temp.tecnologia != null ? temp.tecnologia : "4G"
                    )
                    .densidadePopulacional(temp.concentracaoMedia)
                    .posicao(posicaoGeo)
                    .build();

                entidadesParaSalvar.add(infra);
            }

            // 4. Salvar em lote para máxima performance (Sem deleteAll para evitar deleção em produção)
            // repository.deleteAll();
            repository.saveAll(entidadesParaSalvar);

            log.info(
                "Sucesso! Ingestão concluída de {} registros de infraestrutura de rede no MySQL.",
                entidadesParaSalvar.size()
            );
        } catch (Exception e) {
            log.error("Erro crítico na carga do Dataset Vísent: ", e);
            throw new RuntimeException("Falha na migração dos dados Vísent", e);
        }
    }

    private Map<String, TempAntenna> lerAntenas(String path) throws Exception {
        Map<String, TempAntenna> map = new HashMap<>();
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            throw new IllegalArgumentException(
                "Arquivo de antenas não encontrado: " + path
            );
        }

        try (
            InputStream is = resource.getInputStream();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
            )
        ) {
            String headerLine = reader.readLine();
            if (headerLine == null) return map;

            String separator = detectarSeparador(headerLine);
            String[] headers = headerLine.split(separator);

            // Identificar índices das colunas de forma tolerante a variações de nomes
            int idxEcgi = localizarColuna(
                headers,
                "ecgi",
                "id",
                "codigo",
                "estacao"
            );
            int idxLat = localizarColuna(headers, "lat", "latitude", "y");
            int idxLng = localizarColuna(
                headers,
                "lng",
                "lon",
                "longitude",
                "x"
            );
            int idxOperadora = localizarColuna(
                headers,
                "operadora",
                "operator",
                "empresa"
            );
            int idxTecnologia = localizarColuna(
                headers,
                "tecnologia",
                "tipo_tecnologia",
                "tech",
                "tipo"
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(separator);

                String ecgi = obterValor(values, idxEcgi);
                if (ecgi == null || ecgi.isEmpty()) continue;

                TempAntenna antenna = new TempAntenna();
                antenna.ecgi = ecgi;

                String latStr = obterValor(values, idxLat);
                String lngStr = obterValor(values, idxLng);

                if (latStr != null && lngStr != null) {
                    antenna.latitude = Double.parseDouble(
                        latStr.replace(",", ".")
                    );
                    antenna.longitude = Double.parseDouble(
                        lngStr.replace(",", ".")
                    );
                }

                antenna.operadora = obterValor(values, idxOperadora);
                antenna.tecnologia = obterValor(values, idxTecnologia);

                map.put(ecgi, antenna);
            }
        }
        return map;
    }

    private void vincularConcentracao(
        Map<String, TempAntenna> antenasMap,
        String path
    ) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            log.warn(
                "Arquivo de concentração opcional não localizado: {}",
                path
            );
            return;
        }

        try (
            InputStream is = resource.getInputStream();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
            )
        ) {
            String headerLine = reader.readLine();
            if (headerLine == null) return;

            String separator = detectarSeparador(headerLine);
            String[] headers = headerLine.split(separator);

            int idxEcgi = localizarColuna(headers, "ecgi", "id_antena");
            int idxConcentracao = localizarColuna(
                headers,
                "concentracao",
                "n_usuarios",
                "densidade",
                "volume"
            );

            // Para acumular e tirar média de tráfego se houver múltiplos períodos no tensor
            Map<String, List<Double>> valoresAcumulados = new HashMap<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(separator);

                String ecgi = obterValor(values, idxEcgi);
                String concStr = obterValor(values, idxConcentracao);

                if (ecgi != null && concStr != null) {
                    double valor = Double.parseDouble(
                        concStr.replace(",", ".")
                    );
                    valoresAcumulados
                        .computeIfAbsent(ecgi, k -> new ArrayList<>())
                        .add(valor);
                }
            }

            // Atualiza as antenas pré-carregadas com as médias de concentração
            valoresAcumulados.forEach((ecgi, lista) -> {
                TempAntenna antenna = antenasMap.get(ecgi);
                if (antenna != null) {
                    double media = lista
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
                    antenna.concentracaoMedia = media;
                }
            });
        } catch (Exception e) {
            log.error(
                "Erro não-bloqueante ao ler dados de tráfego/concentração: ",
                e
            );
        }
    }

    private String detectarSeparador(String header) {
        if (header.contains(";")) return ";";
        return ",";
    }

    private int localizarColuna(String[] headers, String... possiveisNomes) {
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].trim().toLowerCase();
            for (String p : possiveisNomes) {
                if (h.contains(p.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String obterValor(String[] values, int idx) {
        if (idx >= 0 && idx < values.length) {
            return values[idx].trim().replaceAll("^\"|\"$", ""); // Remove aspas extras
        }
        return null;
    }

    // Classe auxiliar interna para montagem demográfica
    private static class TempAntenna {

        String ecgi;
        Double latitude;
        Double longitude;
        String operadora;
        String tecnologia;
        double concentracaoMedia = 0.0;
    }
}
