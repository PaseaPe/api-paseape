-- =============================================================================
-- PROYECTO TECNOLÓGICO: PaseaPe (Lima 2026)
-- SPRINT 4: MIGRACIÓN V2.3.0 (CALIFICACIONES, OPINIONES, REPORTES, PENALIDADES Y SANCIONES)
-- Motor: MySQL 8 (Aiven Cloud Managed / Local) | Base de datos: paseape_db
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. ACTUALIZACIÓN DE TABLAS Y CATÁLOGOS EXISTENTES
-- -----------------------------------------------------------------------------

ALTER TABLE `paseape_db`.`paseadores`
    ADD COLUMN `total_calificaciones` INT NOT NULL DEFAULT 0 AFTER `calificacion_promedio`,
    ADD COLUMN `suma_calificaciones`  INT NOT NULL DEFAULT 0 AFTER `total_calificaciones`;

INSERT INTO `paseape_db`.`digital_document_type` (
    `id`, `uuid`, `descripcion`, `creado_el`, `creado_por`, `estado`
) VALUES
    (5, paseape_db.uuid_v4(), 'EVIDENCIA_REPORTE', NOW(), 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
    `descripcion` = VALUES(`descripcion`),
    `estado`      = VALUES(`estado`);

INSERT INTO `paseape_db`.`tipo_notificacion` (
    `id`, `uuid`, `descripcion`, `detalle`, `plantilla_asunto`, `creado_el`, `creado_por`, `estado`
) VALUES
    (7,  paseape_db.uuid_v4(), 'CALIFICACION_RECIBIDA', 'Notificación al paseador cuando recibe una calificación u opinión', '¡Has recibido una nueva calificación!', NOW(), 'ADMIN', 1),
    (8,  paseape_db.uuid_v4(), 'REPORTE_RECIBIDO',      'Confirmación al usuario o alerta al equipo administrativo por nuevo reporte', 'Reporte registrado en PaseaPe', NOW(), 'ADMIN', 1),
    (9,  paseape_db.uuid_v4(), 'REPORTE_ACTUALIZADO',   'Notificación al usuario cuando su reporte cambia de estado o se resuelve', 'Actualización sobre tu reporte', NOW(), 'ADMIN', 1),
    (10, paseape_db.uuid_v4(), 'PENALIDAD_APLICADA',    'Alerta automática por infracción operativa detectada en un servicio', 'Aviso de penalidad en tu cuenta', NOW(), 'ADMIN', 1),
    (11, paseape_db.uuid_v4(), 'CUENTA_SANCIONADA',     'Notificación de suspensión temporal o bloqueo permanente de cuenta', 'Estado de tu cuenta PaseaPe', NOW(), 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
    `descripcion`      = VALUES(`descripcion`),
    `detalle`          = VALUES(`detalle`),
    `plantilla_asunto` = VALUES(`plantilla_asunto`),
    `estado`           = VALUES(`estado`);

-- -----------------------------------------------------------------------------
-- 2. LOOKUP TABLES / CATÁLOGOS SPRINT 4
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `paseape_db`.`categoria_reporte` (
    `id`                 INT          NOT NULL,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(60)  NOT NULL,
    `detalle`            VARCHAR(255) NULL,
    `requiere_paseo`     BIT          NOT NULL DEFAULT 1,
    `prioridad_sugerida` TINYINT      NOT NULL DEFAULT 2,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_categoria_reporte_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_categoria_reporte_unq` (`descripcion` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`estado_reporte` (
    `id`              INT          NOT NULL,
    `uuid`            CHAR(36)     NOT NULL,
    `descripcion`     VARCHAR(50)  NOT NULL,
    `detalle`         VARCHAR(255) NULL,
    `es_terminal`     BIT          NOT NULL DEFAULT 0,
    `creado_el`       DATETIME     NOT NULL,
    `actualizado_el`  DATETIME     NULL,
    `creado_por`      VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado`          BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_estado_reporte_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_estado_reporte_unq` (`descripcion` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_penalidad` (
    `id`                         INT          NOT NULL,
    `uuid`                       CHAR(36)     NOT NULL,
    `codigo`                     VARCHAR(60)  NOT NULL,
    `descripcion`                VARCHAR(150) NOT NULL,
    `rol_aplicable`              VARCHAR(20)  NOT NULL,
    `gravedad`                   VARCHAR(20)  NOT NULL,
    `puntos`                     INT          NOT NULL,
    `dias_vigencia_puntos`       INT          NOT NULL DEFAULT 30,
    `dispara_sancion_inmediata`  BIT          NOT NULL DEFAULT 0,
    `horas_suspension_inmediata` INT          NULL,
    `creado_el`                  DATETIME     NOT NULL,
    `actualizado_el`             DATETIME     NULL,
    `creado_por`                 VARCHAR(100) NOT NULL,
    `actualizado_por`            VARCHAR(100) NULL,
    `estado`                     BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_penalidad_unq` (`uuid` ASC),
    UNIQUE INDEX `codigo_tipo_penalidad_unq` (`codigo` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. MÓDULO DE CALIFICACIONES Y OPINIONES (HU32, HU33, HU34)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `paseape_db`.`paseo_calificacion` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `uuid`                CHAR(36)     NOT NULL,
    `solicitud_paseo_id`  BIGINT       NOT NULL,
    `cliente_id`          BIGINT       NOT NULL,
    `paseador_id`         BIGINT       NOT NULL,
    `puntuacion`          TINYINT      NOT NULL,
    `comentario`          TEXT         NULL,
    `fecha_comentario`    DATETIME     NULL,
    `visible`             BIT          NOT NULL DEFAULT 1,
    `motivo_ocultamiento` VARCHAR(255) NULL,
    `creado_el`           DATETIME     NOT NULL,
    `actualizado_el`      DATETIME     NULL,
    `creado_por`          VARCHAR(100) NOT NULL,
    `actualizado_por`     VARCHAR(100) NULL,
    `estado`              BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_paseo_calificacion_unq` (`uuid` ASC),
    UNIQUE INDEX `unq_pc_solicitud_paseo` (`solicitud_paseo_id` ASC),
    INDEX `idx_pc_cliente` (`cliente_id` ASC),
    INDEX `idx_pc_paseador_visible_fecha` (`paseador_id` ASC, `visible` ASC, `estado` ASC, `creado_el` DESC),
    CONSTRAINT `chk_pc_puntuacion` CHECK (`puntuacion` BETWEEN 1 AND 5),
    CONSTRAINT `fk_pc_solicitud`
        FOREIGN KEY (`solicitud_paseo_id`)
        REFERENCES `paseape_db`.`solicitud_paseo` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_pc_cliente`
        FOREIGN KEY (`cliente_id`)
        REFERENCES `paseape_db`.`clientes` (`id`)
        ON UPDATE CASCADE,
    CONSTRAINT `fk_pc_paseador`
        FOREIGN KEY (`paseador_id`)
        REFERENCES `paseape_db`.`paseadores` (`id`)
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. MÓDULO DE REPORTES Y RECLAMOS (HU37 + BASE HU35/HU36)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `paseape_db`.`reporte_incidencia` (
    `id`                         BIGINT       NOT NULL AUTO_INCREMENT,
    `uuid`                       CHAR(36)     NOT NULL,
    `codigo_reporte`             VARCHAR(30)  NOT NULL,
    `solicitud_paseo_id`         BIGINT       NULL,
    `usuario_reportante_id`      BIGINT       NOT NULL,
    `usuario_reportado_id`       BIGINT       NULL,
    `categoria_reporte_id`       INT          NOT NULL,
    `estado_reporte_id`          INT          NOT NULL,
    `administrador_asignado_id`  BIGINT       NULL,
    `titulo`                     VARCHAR(150) NOT NULL,
    `descripcion`                TEXT         NOT NULL,
    `resolucion_resumen`         TEXT         NULL,
    `fecha_resolucion`           DATETIME     NULL,
    `folder_digital_document_id` BIGINT       NULL,
    `creado_el`                  DATETIME     NOT NULL,
    `actualizado_el`             DATETIME     NULL,
    `creado_por`                 VARCHAR(100) NOT NULL,
    `actualizado_por`            VARCHAR(100) NULL,
    `estado`                     BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_reporte_incidencia_unq` (`uuid` ASC),
    UNIQUE INDEX `codigo_reporte_unq` (`codigo_reporte` ASC),
    INDEX `idx_rep_estado_fecha` (`estado_reporte_id` ASC, `estado` ASC, `creado_el` DESC),
    INDEX `idx_rep_categoria` (`categoria_reporte_id` ASC),
    INDEX `idx_rep_solicitud` (`solicitud_paseo_id` ASC),
    INDEX `idx_rep_reportante` (`usuario_reportante_id` ASC),
    INDEX `idx_rep_reportado` (`usuario_reportado_id` ASC),
    INDEX `idx_rep_admin` (`administrador_asignado_id` ASC),
    CONSTRAINT `fk_rep_solicitud`
        FOREIGN KEY (`solicitud_paseo_id`)
        REFERENCES `paseape_db`.`solicitud_paseo` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_rep_reportante`
        FOREIGN KEY (`usuario_reportante_id`)
        REFERENCES `paseape_db`.`usuarios` (`id`)
        ON UPDATE CASCADE,
    CONSTRAINT `fk_rep_reportado`
        FOREIGN KEY (`usuario_reportado_id`)
        REFERENCES `paseape_db`.`usuarios` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_rep_categoria`
        FOREIGN KEY (`categoria_reporte_id`)
        REFERENCES `paseape_db`.`categoria_reporte` (`id`)
        ON UPDATE CASCADE,
    CONSTRAINT `fk_rep_estado`
        FOREIGN KEY (`estado_reporte_id`)
        REFERENCES `paseape_db`.`estado_reporte` (`id`)
        ON UPDATE CASCADE,
    CONSTRAINT `fk_rep_admin`
        FOREIGN KEY (`administrador_asignado_id`)
        REFERENCES `paseape_db`.`administradores` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_rep_folder_document`
        FOREIGN KEY (`folder_digital_document_id`)
        REFERENCES `paseape_db`.`folder_digital_document` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`reporte_seguimiento` (
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT,
    `uuid`                  CHAR(36)     NOT NULL,
    `reporte_incidencia_id` BIGINT       NOT NULL,
    `administrador_id`      BIGINT       NOT NULL,
    `estado_anterior_id`    INT          NULL,
    `estado_nuevo_id`       INT          NOT NULL,
    `comentario_interno`    TEXT         NOT NULL,
    `visible_usuario`       BIT          NOT NULL DEFAULT 0,
    `creado_el`             DATETIME     NOT NULL,
    `actualizado_el`        DATETIME     NULL,
    `creado_por`            VARCHAR(100) NOT NULL,
    `actualizado_por`       VARCHAR(100) NULL,
    `estado`                BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_reporte_seguimiento_unq` (`uuid` ASC),
    INDEX `idx_rs_reporte_fecha` (`reporte_incidencia_id` ASC, `creado_el` ASC),
    INDEX `idx_rs_admin` (`administrador_id` ASC),
    CONSTRAINT `fk_rs_reporte`
        FOREIGN KEY (`reporte_incidencia_id`)
        REFERENCES `paseape_db`.`reporte_incidencia` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_rs_admin`
        FOREIGN KEY (`administrador_id`)
        REFERENCES `paseape_db`.`administradores` (`id`)
        ON UPDATE CASCADE,
    CONSTRAINT `fk_rs_estado_anterior`
        FOREIGN KEY (`estado_anterior_id`)
        REFERENCES `paseape_db`.`estado_reporte` (`id`)
        ON UPDATE CASCADE,
    CONSTRAINT `fk_rs_estado_nuevo`
        FOREIGN KEY (`estado_nuevo_id`)
        REFERENCES `paseape_db`.`estado_reporte` (`id`)
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. MÓDULO DE SANCIONES Y PENALIDADES AUTOMÁTICAS (HU40 + MOTOR DE FALTAS)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `paseape_db`.`usuario_sancion` (
    `id`                     BIGINT       NOT NULL AUTO_INCREMENT,
    `uuid`                   CHAR(36)     NOT NULL,
    `usuario_id`             BIGINT       NOT NULL,
    `tipo_sancion`           VARCHAR(20)  NOT NULL,
    `origen_sancion`         VARCHAR(30)  NOT NULL,
    `motivo`                 TEXT         NOT NULL,
    `fecha_inicio`           DATETIME     NOT NULL,
    `fecha_fin`              DATETIME     NULL,
    `activa`                 BIT          NOT NULL DEFAULT 1,
    `reporte_incidencia_id`  BIGINT       NULL,
    `administrador_id`       BIGINT       NULL,
    `levantada_el`           DATETIME     NULL,
    `levantada_por_admin_id` BIGINT       NULL,
    `motivo_levantamiento`   VARCHAR(255) NULL,
    `creado_el`              DATETIME     NOT NULL,
    `actualizado_el`         DATETIME     NULL,
    `creado_por`             VARCHAR(100) NOT NULL,
    `actualizado_por`        VARCHAR(100) NULL,
    `estado`                 BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_usuario_sancion_unq` (`uuid` ASC),
    INDEX `idx_us_usuario_activa` (`usuario_id` ASC, `activa` ASC, `estado` ASC),
    INDEX `idx_us_expiracion` (`tipo_sancion` ASC, `activa` ASC, `fecha_fin` ASC),
    INDEX `idx_us_reporte` (`reporte_incidencia_id` ASC),
    CONSTRAINT `fk_us_usuario`
        FOREIGN KEY (`usuario_id`)
        REFERENCES `paseape_db`.`usuarios` (`id`)
        ON UPDATE CASCADE,
    CONSTRAINT `fk_us_reporte`
        FOREIGN KEY (`reporte_incidencia_id`)
        REFERENCES `paseape_db`.`reporte_incidencia` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_us_admin`
        FOREIGN KEY (`administrador_id`)
        REFERENCES `paseape_db`.`administradores` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_us_admin_levantamiento`
        FOREIGN KEY (`levantada_por_admin_id`)
        REFERENCES `paseape_db`.`administradores` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`usuario_penalidad` (
    `id`                      BIGINT       NOT NULL AUTO_INCREMENT,
    `uuid`                    CHAR(36)     NOT NULL,
    `usuario_id`              BIGINT       NOT NULL,
    `tipo_penalidad_id`       INT          NOT NULL,
    `solicitud_paseo_id`      BIGINT       NULL,
    `usuario_sancion_id`      BIGINT       NULL,
    `puntos_aplicados`        INT          NOT NULL,
    `fecha_expiracion_puntos` DATETIME     NOT NULL,
    `origen_deteccion`        VARCHAR(30)  NOT NULL,
    `detalle_evidencia`       TEXT         NULL,
    `anulada`                 BIT          NOT NULL DEFAULT 0,
    `anulada_el`              DATETIME     NULL,
    `anulada_por_admin_id`    BIGINT       NULL,
    `motivo_anulacion`        VARCHAR(255) NULL,
    `creado_el`               DATETIME     NOT NULL,
    `actualizado_el`          DATETIME     NULL,
    `creado_por`              VARCHAR(100) NOT NULL,
    `actualizado_por`         VARCHAR(100) NULL,
    `estado`                  BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_usuario_penalidad_unq` (`uuid` ASC),
    UNIQUE INDEX `unq_up_solicitud_usuario_tipo` (`solicitud_paseo_id` ASC, `usuario_id` ASC, `tipo_penalidad_id` ASC),
    INDEX `idx_up_usuario_vigencia` (`usuario_id` ASC, `anulada` ASC, `estado` ASC, `fecha_expiracion_puntos` ASC),
    INDEX `idx_up_tipo_penalidad` (`tipo_penalidad_id` ASC),
    INDEX `idx_up_sancion` (`usuario_sancion_id` ASC),
    CONSTRAINT `fk_up_usuario`
        FOREIGN KEY (`usuario_id`)
        REFERENCES `paseape_db`.`usuarios` (`id`)
        ON UPDATE CASCADE,
    CONSTRAINT `fk_up_tipo_penalidad`
        FOREIGN KEY (`tipo_penalidad_id`)
        REFERENCES `paseape_db`.`tipo_penalidad` (`id`)
        ON UPDATE CASCADE,
    CONSTRAINT `fk_up_solicitud`
        FOREIGN KEY (`solicitud_paseo_id`)
        REFERENCES `paseape_db`.`solicitud_paseo` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_up_sancion`
        FOREIGN KEY (`usuario_sancion_id`)
        REFERENCES `paseape_db`.`usuario_sancion` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_up_admin_anulacion`
        FOREIGN KEY (`anulada_por_admin_id`)
        REFERENCES `paseape_db`.`administradores` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. CARGA INICIAL DE CATÁLOGOS SPRINT 4
-- -----------------------------------------------------------------------------

INSERT INTO `paseape_db`.`categoria_reporte` (
    `id`, `uuid`, `descripcion`, `detalle`, `requiere_paseo`, `prioridad_sugerida`, `creado_el`, `creado_por`, `estado`
) VALUES
    (1, paseape_db.uuid_v4(), 'MALTRATO_O_RIESGO_MASCOTA', 'Incidentes de integridad física, extravío o trato inadecuado hacia la mascota', 1, 1, NOW(), 'ADMIN', 1),
    (2, paseape_db.uuid_v4(), 'INCUMPLIMIENTO_HORARIO',      'Retraso severo, no presentación (no-show) o cancelación injustificada',          1, 2, NOW(), 'ADMIN', 1),
    (3, paseape_db.uuid_v4(), 'PASEO_INCOMPLETO',            'Servicio finalizado antes del tiempo contratado o sin recorrido válido',           1, 2, NOW(), 'ADMIN', 1),
    (4, paseape_db.uuid_v4(), 'COMPORTAMIENTO_INAPROPIADO',  'Faltas de respeto, acoso o conducta inadecuada entre usuarios',                     0, 1, NOW(), 'ADMIN', 1),
    (5, paseape_db.uuid_v4(), 'PROBLEMA_PAGO',               'Inconvenientes con el cobro en efectivo o Yape/Plin acordado',                     1, 2, NOW(), 'ADMIN', 1),
    (6, paseape_db.uuid_v4(), 'OTRO',                        'Otros reclamos operativos o técnicos reportados por el usuario',                     0, 3, NOW(), 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
    `descripcion`        = VALUES(`descripcion`),
    `detalle`            = VALUES(`detalle`),
    `requiere_paseo`     = VALUES(`requiere_paseo`),
    `prioridad_sugerida` = VALUES(`prioridad_sugerida`),
    `estado`             = VALUES(`estado`);

INSERT INTO `paseape_db`.`estado_reporte` (
    `id`, `uuid`, `descripcion`, `detalle`, `es_terminal`, `creado_el`, `creado_por`, `estado`
) VALUES
    (1, paseape_db.uuid_v4(), 'PENDIENTE',   'Reporte ingresado, pendiente de asignación o revisión administrativa', 0, NOW(), 'ADMIN', 1),
    (2, paseape_db.uuid_v4(), 'EN_REVISION', 'Reporte en investigación activa por parte de un administrador',        0, NOW(), 'ADMIN', 1),
    (3, paseape_db.uuid_v4(), 'RESUELTO',    'Reporte atendido y cerrado con resolución o sanción aplicada',         1, NOW(), 'ADMIN', 1),
    (4, paseape_db.uuid_v4(), 'DESESTIMADO', 'Reporte cerrado sin evidencia suficiente o improcedente',              1, NOW(), 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
    `descripcion` = VALUES(`descripcion`),
    `detalle`     = VALUES(`detalle`),
    `es_terminal` = VALUES(`es_terminal`),
    `estado`      = VALUES(`estado`);

INSERT INTO `paseape_db`.`tipo_penalidad` (
    `id`, `uuid`, `codigo`, `descripcion`, `rol_aplicable`, `gravedad`, `puntos`, `dias_vigencia_puntos`, `dispara_sancion_inmediata`, `horas_suspension_inmediata`, `creado_el`, `creado_por`, `estado`
) VALUES
    (1, paseape_db.uuid_v4(), 'CANCELACION_TARDIA_PASEADOR', 'Cancelación de paseo confirmado con menos de 2h o fuera de gracia',    'PASEADOR', 'MODERADA', 2, 30, 0, NULL, NOW(), 'ADMIN', 1),
    (2, paseape_db.uuid_v4(), 'CANCELACION_TARDIA_CLIENTE',  'Cancelación de paseo confirmado con menos de 1h o fuera de gracia',    'CLIENTE',  'LEVE',     1, 30, 0, NULL, NOW(), 'ADMIN', 1),
    (3, paseape_db.uuid_v4(), 'NO_SHOW_PASEADOR',            'Paseador confirmado no inició el paseo dentro de la ventana de tolerancia', 'PASEADOR', 'GRAVE', 3, 30, 0, NULL, NOW(), 'ADMIN', 1),
    (4, paseape_db.uuid_v4(), 'NO_SHOW_CLIENTE',             'Cliente no entregó la mascota dentro del tiempo de espera en recojo',  'CLIENTE',  'MODERADA', 2, 30, 0, NULL, NOW(), 'ADMIN', 1),
    (5, paseape_db.uuid_v4(), 'FINALIZACION_ANTICIPADA',     'Paseo finalizado con menos del 80% de la duración contratada',         'PASEADOR', 'MODERADA', 2, 30, 0, NULL, NOW(), 'ADMIN', 1),
    (6, paseape_db.uuid_v4(), 'TELEMETRIA_INSUFICIENTE',     'Paseo >= 15 min sin puntos GPS válidos o desplazamiento menor a 50 m', 'PASEADOR', 'LEVE',     1, 30, 0, NULL, NOW(), 'ADMIN', 1),
    (7, paseape_db.uuid_v4(), 'REPORTE_GRAVE_CONFIRMADO',    'Infracción grave confirmada por administración tras reporte',          'AMBOS',    'CRITICA',  5, 60, 1, 720,  NOW(), 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
    `codigo`                     = VALUES(`codigo`),
    `descripcion`                = VALUES(`descripcion`),
    `rol_aplicable`              = VALUES(`rol_aplicable`),
    `gravedad`                   = VALUES(`gravedad`),
    `puntos`                     = VALUES(`puntos`),
    `dias_vigencia_puntos`       = VALUES(`dias_vigencia_puntos`),
    `dispara_sancion_inmediata`  = VALUES(`dispara_sancion_inmediata`),
    `horas_suspension_inmediata` = VALUES(`horas_suspension_inmediata`),
    `estado`                     = VALUES(`estado`);
