-- =============================================================================
-- PROYECTO TECNOLÓGICO: PaseaPe (Lima 2026)
-- SPRINT 4: MIGRACIÓN V2.3.1 (CATÁLOGO DE MOTIVOS DE CANCELACIÓN PARAMETRIZADOS)
-- Motor: MySQL 8 (Aiven Cloud Managed / Local) | Base de datos: paseape_db
-- =============================================================================

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_motivo_cancelacion` (
    `id`                  INT          NOT NULL,
    `uuid`                CHAR(36)     NOT NULL,
    `descripcion`         VARCHAR(120) NOT NULL,
    `rol_aplicable`       VARCHAR(20)  NOT NULL,
    `tipo_penalidad_id`   INT          NULL,
    `requiere_comentario` BIT          NOT NULL DEFAULT 1,
    `creado_el`           DATETIME     NOT NULL,
    `actualizado_el`      DATETIME     NULL,
    `creado_por`          VARCHAR(100) NOT NULL,
    `actualizado_por`     VARCHAR(100) NULL,
    `estado`              BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_motivo_cancelacion_unq` (`uuid` ASC),
    UNIQUE INDEX `uk_tmc_rol_descripcion` (`rol_aplicable` ASC, `descripcion` ASC),
    INDEX `idx_tmc_tipo_penalidad` (`tipo_penalidad_id` ASC),
    CONSTRAINT `fk_tmc_tipo_penalidad`
        FOREIGN KEY (`tipo_penalidad_id`)
        REFERENCES `paseape_db`.`tipo_penalidad` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO `paseape_db`.`tipo_motivo_cancelacion` (
    `id`, `uuid`, `descripcion`, `rol_aplicable`, `tipo_penalidad_id`, `requiere_comentario`, `creado_el`, `creado_por`, `estado`
) VALUES
    (1, paseape_db.uuid_v4(), 'Cliente no entregó la mascota (+15 min de espera)', 'PASEADOR', 4,    1, NOW(), 'ADMIN', 1),
    (2, paseape_db.uuid_v4(), 'Emergencia personal / No podré asistir',              'PASEADOR', 1,    1, NOW(), 'ADMIN', 1),
    (3, paseape_db.uuid_v4(), 'Motivo de fuerza mayor / clima extremo',              'PASEADOR', NULL, 1, NOW(), 'ADMIN', 1),
    (4, paseape_db.uuid_v4(), 'El paseador no responde o tiene retraso',             'CLIENTE',  NULL, 1, NOW(), 'ADMIN', 1),
    (5, paseape_db.uuid_v4(), 'Cambio de planes / Ya no requiero el paseo',          'CLIENTE',  2,    1, NOW(), 'ADMIN', 1),
    (6, paseape_db.uuid_v4(), 'Error al seleccionar fecha, hora o mascotas',         'CLIENTE',  2,    1, NOW(), 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
    `descripcion`         = VALUES(`descripcion`),
    `rol_aplicable`       = VALUES(`rol_aplicable`),
    `tipo_penalidad_id`   = VALUES(`tipo_penalidad_id`),
    `requiere_comentario` = VALUES(`requiere_comentario`),
    `estado`              = VALUES(`estado`);
