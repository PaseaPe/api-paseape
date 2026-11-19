package com.paseape.apipaseape.infrastructure.util;

import com.paseape.apipaseape.infrastructure.constant.Constant;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class UtilMethods {

    private UtilMethods() {
    }

    public static LocalDateTime convertirUtcALima(LocalDateTime fechaUtc) {
        if (fechaUtc == null) {
            return null;
        }
        return fechaUtc.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of(Constant.ZONE_ID_LIMA))
                .toLocalDateTime();
    }
}
