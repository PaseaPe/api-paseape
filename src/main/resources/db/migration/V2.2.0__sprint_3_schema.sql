-- =============================================================================
-- PROYECTO TECNOLÓGICO: PaseaPe (Lima 2026)
-- SPRINT 3: MIGRACIÓN V2.2.0 (TELEMETRÍA GPS ACUMULATIVA Y RECORRIDO DE PASEO)
-- Motor: MySQL 8 (Aiven Cloud Managed / XAMPP Local) | Base de datos: paseape_db
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. CATÁLOGO: TIPO ORIGEN TRAZA
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_origen_traza` (
    `id`              INT          NOT NULL,
    `uuid`            CHAR(36)     NOT NULL,
    `descripcion`     VARCHAR(50)  NOT NULL,
    `detalle`         VARCHAR(255) NULL,
    `creado_el`       DATETIME     NOT NULL,
    `actualizado_el`  DATETIME     NULL,
    `creado_por`      VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado`          BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_origen_traza_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_origen_traza_unq` (`descripcion` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO `paseape_db`.`tipo_origen_traza` (
    `id`, `uuid`, `descripcion`, `detalle`, `creado_el`, `creado_por`, `estado`
) VALUES
    (1, paseape_db.uuid_v4(), 'GPS_TIEMPO_REAL',      'Telemetría GPS en tiempo real capturada en la sesión del paseo', NOW(), 'ADMIN', 1),
    (2, paseape_db.uuid_v4(), 'RUTA_INTERPOLADA',     'Ruta aproximada interpolada ante pérdidas o interrupciones de señal', NOW(), 'ADMIN', 1),
    (3, paseape_db.uuid_v4(), 'CIERRE_ADMINISTRATIVO', 'Cierre manual asistido o regularizado por el equipo de soporte', NOW(), 'ADMIN', 1)
ON DUPLICATE KEY UPDATE `descripcion` = VALUES(`descripcion`), `detalle` = VALUES(`detalle`);

-- -----------------------------------------------------------------------------
-- 2. BUFFER EN VIVO: SOLICITUD PASEO UBICACIÓN EN VIVO (1 FILA POR PASEO EN CURSO)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `paseape_db`.`solicitud_paseo_ubicacion_en_vivo` (
    `id`                         BIGINT         NOT NULL AUTO_INCREMENT,
    `uuid`                       CHAR(36)       NOT NULL,
    `solicitud_paseo_id`         BIGINT         NOT NULL,
    `latitud_actual`             DECIMAL(10, 8) NOT NULL,
    `longitud_actual`            DECIMAL(11, 8) NOT NULL,
    `precision_m`                DECIMAL(6, 2)  NULL,
    `velocidad_ms`               DECIMAL(6, 2)  NULL,
    `rumbo_grados`               DECIMAL(5, 2)  NULL,
    `fecha_hora_gps`             DATETIME(6)    NOT NULL,
    `coordenadas_codificadas`    LONGTEXT       NOT NULL,
    `distancia_metros_acumulada` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `total_puntos`               INT            NOT NULL DEFAULT 1,
    `creado_el`                  DATETIME       NOT NULL,
    `actualizado_el`             DATETIME       NULL,
    `creado_por`                 VARCHAR(100)   NOT NULL,
    `actualizado_por`            VARCHAR(100)   NULL,
    `estado`                     BIT            NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_spuev_unq` (`uuid` ASC),
    UNIQUE INDEX `solicitud_spuev_unq` (`solicitud_paseo_id` ASC),
    INDEX `idx_spuev_solicitud_estado` (`solicitud_paseo_id` ASC, `estado` ASC),
    CONSTRAINT `fk_spuev_solicitud`
        FOREIGN KEY (`solicitud_paseo_id`)
        REFERENCES `paseape_db`.`solicitud_paseo` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. HISTÓRICO: SOLICITUD PASEO RECORRIDO (HISTORIAL STRAVA-LIKE CONSOLIDADO)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `paseape_db`.`solicitud_paseo_recorrido` (
    `id`                       BIGINT         NOT NULL AUTO_INCREMENT,
    `uuid`                     CHAR(36)       NOT NULL,
    `solicitud_paseo_id`       BIGINT         NOT NULL,
    `tipo_origen_traza_id`     INT            NOT NULL,
    `distancia_metros`         DECIMAL(10, 2) NOT NULL,
    `duracion_segundos`        INT            NOT NULL,
    `velocidad_promedio_kmh`   DECIMAL(5, 2)  NULL,
    `coordenadas_codificadas`  LONGTEXT       NOT NULL,
    `coordenadas_total_puntos` INT            NOT NULL,
    `creado_el`                DATETIME       NOT NULL,
    `actualizado_el`           DATETIME       NULL,
    `creado_por`               VARCHAR(100)   NOT NULL,
    `actualizado_por`          VARCHAR(100)   NULL,
    `estado`                   BIT            NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_spr_unq` (`uuid` ASC),
    UNIQUE INDEX `solicitud_spr_unq` (`solicitud_paseo_id` ASC),
    INDEX `idx_spr_solicitud_estado` (`solicitud_paseo_id` ASC, `estado` ASC),
    INDEX `idx_spr_tipo_origen` (`tipo_origen_traza_id` ASC),
    CONSTRAINT `fk_spr_solicitud`
        FOREIGN KEY (`solicitud_paseo_id`)
        REFERENCES `paseape_db`.`solicitud_paseo` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `fk_spr_tipo_origen`
        FOREIGN KEY (`tipo_origen_traza_id`)
        REFERENCES `paseape_db`.`tipo_origen_traza` (`id`)
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
