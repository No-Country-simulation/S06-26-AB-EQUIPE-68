package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.NetworkStatusDto;
import org.locationtech.jts.geom.Point;

public interface GeolocationService {
    /**
     * Avalia o status de conectividade da rede para um determinado usuário com base na sua localização.
     *
     * @param usuarioId O ID do usuário cuja localização será usada para verificar o status da rede.
     * @param raioMetros O raio em metros para buscar torres de rede próximas.
     * @return Um objeto NetworkStatusDto contendo o status da rede (Estável/Instável) e a tecnologia predominante.
     */
    NetworkStatusDto getNetworkStatus(Long usuarioId, double raioMetros);

    /**
     * Cria um objeto Point a partir de latitude e longitude.
     * @param latitude Latitude do ponto.
     * @param longitude Longitude do ponto.
     * @return Objeto Point com SRID 4326.
     */
    Point createPoint(double latitude, double longitude);
}
