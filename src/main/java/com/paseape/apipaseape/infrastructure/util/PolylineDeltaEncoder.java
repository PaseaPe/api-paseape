package com.paseape.apipaseape.infrastructure.util;

import java.math.BigDecimal;

public final class PolylineDeltaEncoder {

    private PolylineDeltaEncoder() {
    }

    public static String codificarPuntoInicial(BigDecimal latitud, BigDecimal longitud) {
        if (latitud == null || longitud == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        encodeCoordinate(Math.round(latitud.doubleValue() * 1e5), sb);
        encodeCoordinate(Math.round(longitud.doubleValue() * 1e5), sb);
        return sb.toString();
    }

    public static String concatenarPuntoDelta(BigDecimal latAnterior, BigDecimal lngAnterior,
                                             BigDecimal latNueva, BigDecimal lngNueva) {
        if (latAnterior == null || lngAnterior == null || latNueva == null || lngNueva == null) {
            return "";
        }
        long dLat = Math.round(latNueva.doubleValue() * 1e5) - Math.round(latAnterior.doubleValue() * 1e5);
        long dLng = Math.round(lngNueva.doubleValue() * 1e5) - Math.round(lngAnterior.doubleValue() * 1e5);

        StringBuilder sb = new StringBuilder();
        encodeCoordinate(dLat, sb);
        encodeCoordinate(dLng, sb);
        return sb.toString();
    }

    private static void encodeCoordinate(long val, StringBuilder result) {
        long v = val < 0 ? ~(val << 1) : (val << 1);
        while (v >= 0x20) {
            result.append((char) ((0x20 | (v & 0x1f)) + 63));
            v >>= 5;
        }
        result.append((char) (v + 63));
    }
}
