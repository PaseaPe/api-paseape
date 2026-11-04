-- =============================================================================
-- PROYECTO TECNOLÓGICO: PaseaPe (Lima 2026)
-- SPRINT 2: MIGRACIÓN V2.0.4 (TABLA DE DESCARTE DE SOLICITUDES INMEDIATAS)
-- Motor: MySQL 8 | Base de datos: paseape_db
-- =============================================================================

CREATE TABLE IF NOT EXISTS `paseape_db`.`solicitud_paseo_descarte` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `solicitud_paseo_id` BIGINT NOT NULL,
    `paseador_id` BIGINT NOT NULL,
    `motivo_descarte` VARCHAR(255) NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_solicitud_paseo_descarte_unq` (`uuid` ASC),
    UNIQUE INDEX `unq_solicitud_paseador_descarte` (
        `solicitud_paseo_id` ASC,
        `paseador_id` ASC
    ),
    INDEX `idx_spd_paseador_solicitud` (`paseador_id` ASC, `solicitud_paseo_id` ASC),
    CONSTRAINT `fk_spd_solicitud` FOREIGN KEY (`solicitud_paseo_id`) REFERENCES `paseape_db`.`solicitud_paseo` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_spd_paseador` FOREIGN KEY (`paseador_id`) REFERENCES `paseape_db`.`paseadores` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
