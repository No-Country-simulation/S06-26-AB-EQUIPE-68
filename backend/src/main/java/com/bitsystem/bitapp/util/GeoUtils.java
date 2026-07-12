package com.bitsystem.bitapp.util;

/**
 * ============================================================================
 * UTILITÁRIO: GeoUtils
 * ============================================================================
 *
 * Cálculo de distância geográfica (Haversine), extraído de
 * GeolocationServiceImpl para ser reutilizado por outros serviços que também
 * precisam medir distância a antenas/pontos (ex.: LazerService).
 *
 * @author BiT System
 * @version 1.0.0
 */
public final class GeoUtils {

    private GeoUtils() {}

    /** Raio médio da Terra, em metros. */
    private static final int RAIO_TERRA_METROS = 6371000;

    public static double distanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RAIO_TERRA_METROS * c;
    }
}