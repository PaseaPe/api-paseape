-- =============================================================================
-- PROYECTO TECNOLÓGICO: PaseaPe (Lima 2026)
-- SPRINT 2: MIGRACIÓN V2.0.3 (PASEOS, DISPONIBILIDAD, SOLICITUDES Y NOTIFICACIONES)
-- Motor: MySQL 8 | Base de datos: paseape_db
-- =============================================================================
-- -----------------------------------------------------------------------------
-- 1. ACTUALIZACIÓN DE TABLA EXISTENTE: PASEADORES
-- -----------------------------------------------------------------------------
ALTER TABLE `paseape_db`.`paseadores`
ADD COLUMN `max_mascotas_predeterminado` INT NOT NULL DEFAULT 3 AFTER `calificacion_promedio`;

-- -----------------------------------------------------------------------------
-- 2. LOOKUP TABLES / CATÁLOGOS SPRINT 2
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `paseape_db`.`dia_semana` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `descripcion` VARCHAR(30) NOT NULL,
    `numero_dia` INT NOT NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_dia_semana_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_dia_semana_unq` (`descripcion` ASC),
    UNIQUE INDEX `numero_dia_semana_unq` (`numero_dia` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`bloque_horario` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `hora_inicio` TIME NOT NULL,
    `hora_fin` TIME NOT NULL,
    `codigo` VARCHAR(30) NOT NULL,
    `orden` INT NOT NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_bloque_horario_unq` (`uuid` ASC),
    UNIQUE INDEX `codigo_bloque_horario_unq` (`codigo` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_duracion_paseo` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `descripcion` VARCHAR(50) NOT NULL,
    `minutos` INT NOT NULL,
    `bloques_requeridos` INT NOT NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_duracion_paseo_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_duracion_paseo_unq` (`descripcion` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_modalidad_paseo` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `descripcion` VARCHAR(50) NOT NULL,
    `detalle` VARCHAR(255) NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_modalidad_paseo_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_modalidad_paseo_unq` (`descripcion` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_metodo_pago` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `descripcion` VARCHAR(50) NOT NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_metodo_pago_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_metodo_pago_unq` (`descripcion` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`solicitud_paseo_estado` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `descripcion` VARCHAR(50) NOT NULL,
    `detalle` VARCHAR(255) NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_solicitud_paseo_estado_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_solicitud_paseo_estado_unq` (`descripcion` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_notificacion` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `descripcion` VARCHAR(50) NOT NULL,
    `detalle` VARCHAR(255) NULL,
    `plantilla_asunto` VARCHAR(200) NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_notificacion_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_notificacion_unq` (`descripcion` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. TABLAS TRANSACCIONALES
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `paseape_db`.`disponibilidad_paseador` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `paseador_id` BIGINT NOT NULL,
    `dia_semana_id` INT NOT NULL,
    `bloque_horario_id` INT NOT NULL,
    `max_mascotas` INT NOT NULL DEFAULT 3,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_disponibilidad_paseador_unq` (`uuid` ASC),
    UNIQUE INDEX `unq_disponibilidad_paseador_slot` (
        `paseador_id` ASC,
        `dia_semana_id` ASC,
        `bloque_horario_id` ASC
    ),
    INDEX `idx_disp_paseador` (`paseador_id` ASC),
    INDEX `idx_disp_dia_semana` (`dia_semana_id` ASC),
    INDEX `idx_disp_bloque_horario` (`bloque_horario_id` ASC),
    CONSTRAINT `fk_disp_paseadores` FOREIGN KEY (`paseador_id`) REFERENCES `paseape_db`.`paseadores` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_disp_dia_semana` FOREIGN KEY (`dia_semana_id`) REFERENCES `paseape_db`.`dia_semana` (`id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_disp_bloque_horario` FOREIGN KEY (`bloque_horario_id`) REFERENCES `paseape_db`.`bloque_horario` (`id`) ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`solicitud_paseo` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `codigo_reserva` VARCHAR(50) NOT NULL,
    `tipo_modalidad_paseo_id` INT NOT NULL,
    `tipo_metodo_pago_id` INT NOT NULL,
    `cliente_id` BIGINT NOT NULL,
    `paseador_id` BIGINT NULL,
    `distrito_id` INT NOT NULL,
    `direccion_recojo` VARCHAR(255) NOT NULL,
    `referencia_direccion` VARCHAR(255) NULL,
    `fecha_programada` DATE NULL,
    `bloque_horario_id` INT NULL,
    `tipo_duracion_paseo_id` INT NOT NULL,
    `tarifa_hora_pen_snapshot` DECIMAL(10, 2) NOT NULL,
    `monto_total` DECIMAL(10, 2) NOT NULL,
    `solicitud_paseo_estado_id` INT NOT NULL,
    `motivo_rechazo_cancelacion` TEXT NULL,
    `fecha_hora_aceptada` DATETIME NULL,
    `fecha_hora_inicio_real` DATETIME NULL,
    `fecha_hora_fin_real` DATETIME NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_solicitud_paseo_unq` (`uuid` ASC),
    UNIQUE INDEX `codigo_reserva_solicitud_paseo_unq` (`codigo_reserva` ASC),
    INDEX `idx_sol_cliente` (`cliente_id` ASC),
    INDEX `idx_sol_paseador` (`paseador_id` ASC),
    INDEX `idx_sol_distrito` (`distrito_id` ASC),
    INDEX `idx_sol_bloque` (`bloque_horario_id` ASC),
    INDEX `idx_sol_estado` (
        `solicitud_paseo_estado_id` ASC
    ),
    INDEX `idx_sol_fecha_bloque` (
        `fecha_programada` ASC,
        `bloque_horario_id` ASC
    ),
    CONSTRAINT `fk_sol_modalidad` FOREIGN KEY (`tipo_modalidad_paseo_id`) REFERENCES `paseape_db`.`tipo_modalidad_paseo` (`id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_sol_metodo_pago` FOREIGN KEY (`tipo_metodo_pago_id`) REFERENCES `paseape_db`.`tipo_metodo_pago` (`id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_sol_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `paseape_db`.`clientes` (`id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_sol_paseador` FOREIGN KEY (`paseador_id`) REFERENCES `paseape_db`.`paseadores` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_sol_distrito` FOREIGN KEY (`distrito_id`) REFERENCES `paseape_db`.`distritos_lima` (`id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_sol_bloque` FOREIGN KEY (`bloque_horario_id`) REFERENCES `paseape_db`.`bloque_horario` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_sol_duracion` FOREIGN KEY (`tipo_duracion_paseo_id`) REFERENCES `paseape_db`.`tipo_duracion_paseo` (`id`) ON UPDATE CASCADE,
    CONSTRAINT `fk_sol_estado` FOREIGN KEY (`solicitud_paseo_estado_id`) REFERENCES `paseape_db`.`solicitud_paseo_estado` (`id`) ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`solicitud_paseo_mascota` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `solicitud_paseo_id` BIGINT NOT NULL,
    `mascota_id` BIGINT NOT NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_solicitud_paseo_mascota_unq` (`uuid` ASC),
    UNIQUE INDEX `unq_solicitud_paseo_mascota` (
        `solicitud_paseo_id` ASC,
        `mascota_id` ASC
    ),
    INDEX `idx_spm_solicitud` (`solicitud_paseo_id` ASC),
    INDEX `idx_spm_mascota` (`mascota_id` ASC),
    CONSTRAINT `fk_spm_solicitud` FOREIGN KEY (`solicitud_paseo_id`) REFERENCES `paseape_db`.`solicitud_paseo` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_spm_mascota` FOREIGN KEY (`mascota_id`) REFERENCES `paseape_db`.`mascotas` (`id`) ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`notificaciones` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36) NOT NULL,
    `remitente_usuario_id` BIGINT NULL,
    `destinatario_usuario_id` BIGINT NOT NULL,
    `tipo_notificacion_id` INT NOT NULL,
    `titulo` VARCHAR(150) NOT NULL,
    `mensaje` TEXT NOT NULL,
    `referencia_id` BIGINT NULL,
    `referencia_tipo` VARCHAR(50) NULL,
    `leido` BIT NOT NULL DEFAULT 0,
    `fecha_leido` DATETIME NULL,
    `creado_el` DATETIME NOT NULL,
    `actualizado_el` DATETIME NULL,
    `creado_por` VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado` BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_notificaciones_unq` (`uuid` ASC),
    INDEX `idx_notif_destinatario` (`destinatario_usuario_id` ASC),
    INDEX `idx_notif_remitente` (`remitente_usuario_id` ASC),
    INDEX `idx_notif_tipo` (`tipo_notificacion_id` ASC),
    INDEX `idx_notif_leido` (
        `destinatario_usuario_id` ASC,
        `leido` ASC
    ),
    CONSTRAINT `fk_notif_remitente` FOREIGN KEY (`remitente_usuario_id`) REFERENCES `paseape_db`.`usuarios` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_notif_destinatario` FOREIGN KEY (`destinatario_usuario_id`) REFERENCES `paseape_db`.`usuarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_notif_tipo` FOREIGN KEY (`tipo_notificacion_id`) REFERENCES `paseape_db`.`tipo_notificacion` (`id`) ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. CARGA DE CATÁLOGOS E INSERTS INICIALES
-- -----------------------------------------------------------------------------
INSERT INTO
    `paseape_db`.`tipo_metodo_pago` (
        `id`,
        `uuid`,
        `descripcion`,
        `creado_el`,
        `creado_por`,
        `estado`
    )
VALUES (
        1,
        paseape_db.uuid_v4 (),
        'YAPE / PLIN',
        NOW(),
        'ADMIN',
        1
    ),
    (
        2,
        paseape_db.uuid_v4 (),
        'EFECTIVO',
        NOW(),
        'ADMIN',
        1
    );

INSERT INTO
    `paseape_db`.`dia_semana` (
        `id`,
        `uuid`,
        `descripcion`,
        `numero_dia`,
        `creado_el`,
        `creado_por`,
        `estado`
    )
VALUES (
        1,
        paseape_db.uuid_v4 (),
        'LUNES',
        1,
        NOW(),
        'ADMIN',
        1
    ),
    (
        2,
        paseape_db.uuid_v4 (),
        'MARTES',
        2,
        NOW(),
        'ADMIN',
        1
    ),
    (
        3,
        paseape_db.uuid_v4 (),
        'MIÉRCOLES',
        3,
        NOW(),
        'ADMIN',
        1
    ),
    (
        4,
        paseape_db.uuid_v4 (),
        'JUEVES',
        4,
        NOW(),
        'ADMIN',
        1
    ),
    (
        5,
        paseape_db.uuid_v4 (),
        'VIERNES',
        5,
        NOW(),
        'ADMIN',
        1
    ),
    (
        6,
        paseape_db.uuid_v4 (),
        'SÁBADO',
        6,
        NOW(),
        'ADMIN',
        1
    ),
    (
        7,
        paseape_db.uuid_v4 (),
        'DOMINGO',
        7,
        NOW(),
        'ADMIN',
        1
    );

INSERT INTO
    `paseape_db`.`bloque_horario` (
        `id`,
        `uuid`,
        `hora_inicio`,
        `hora_fin`,
        `codigo`,
        `orden`,
        `creado_el`,
        `creado_por`,
        `estado`
    )
VALUES (
        1,
        paseape_db.uuid_v4 (),
        '06:00:00',
        '07:00:00',
        'B06_07',
        1,
        NOW(),
        'ADMIN',
        1
    ),
    (
        2,
        paseape_db.uuid_v4 (),
        '07:00:00',
        '08:00:00',
        'B07_08',
        2,
        NOW(),
        'ADMIN',
        1
    ),
    (
        3,
        paseape_db.uuid_v4 (),
        '08:00:00',
        '09:00:00',
        'B08_09',
        3,
        NOW(),
        'ADMIN',
        1
    ),
    (
        4,
        paseape_db.uuid_v4 (),
        '09:00:00',
        '10:00:00',
        'B09_10',
        4,
        NOW(),
        'ADMIN',
        1
    ),
    (
        5,
        paseape_db.uuid_v4 (),
        '10:00:00',
        '11:00:00',
        'B10_11',
        5,
        NOW(),
        'ADMIN',
        1
    ),
    (
        6,
        paseape_db.uuid_v4 (),
        '11:00:00',
        '12:00:00',
        'B11_12',
        6,
        NOW(),
        'ADMIN',
        1
    ),
    (
        7,
        paseape_db.uuid_v4 (),
        '12:00:00',
        '13:00:00',
        'B12_13',
        7,
        NOW(),
        'ADMIN',
        1
    ),
    (
        8,
        paseape_db.uuid_v4 (),
        '13:00:00',
        '14:00:00',
        'B13_14',
        8,
        NOW(),
        'ADMIN',
        1
    ),
    (
        9,
        paseape_db.uuid_v4 (),
        '14:00:00',
        '15:00:00',
        'B14_15',
        9,
        NOW(),
        'ADMIN',
        1
    ),
    (
        10,
        paseape_db.uuid_v4 (),
        '15:00:00',
        '16:00:00',
        'B15_16',
        10,
        NOW(),
        'ADMIN',
        1
    ),
    (
        11,
        paseape_db.uuid_v4 (),
        '16:00:00',
        '17:00:00',
        'B16_17',
        11,
        NOW(),
        'ADMIN',
        1
    ),
    (
        12,
        paseape_db.uuid_v4 (),
        '17:00:00',
        '18:00:00',
        'B17_18',
        12,
        NOW(),
        'ADMIN',
        1
    ),
    (
        13,
        paseape_db.uuid_v4 (),
        '18:00:00',
        '19:00:00',
        'B18_19',
        13,
        NOW(),
        'ADMIN',
        1
    ),
    (
        14,
        paseape_db.uuid_v4 (),
        '19:00:00',
        '20:00:00',
        'B19_20',
        14,
        NOW(),
        'ADMIN',
        1
    ),
    (
        15,
        paseape_db.uuid_v4 (),
        '20:00:00',
        '21:00:00',
        'B20_21',
        15,
        NOW(),
        'ADMIN',
        1
    ),
    (
        16,
        paseape_db.uuid_v4 (),
        '21:00:00',
        '22:00:00',
        'B21_22',
        16,
        NOW(),
        'ADMIN',
        1
    ),
    (
        17,
        paseape_db.uuid_v4 (),
        '22:00:00',
        '23:00:00',
        'B22_23',
        17,
        NOW(),
        'ADMIN',
        1
    );

INSERT INTO
    `paseape_db`.`tipo_duracion_paseo` (
        `id`,
        `uuid`,
        `descripcion`,
        `minutos`,
        `bloques_requeridos`,
        `creado_el`,
        `creado_por`,
        `estado`
    )
VALUES (
        1,
        paseape_db.uuid_v4 (),
        '30 MINUTOS',
        30,
        1,
        NOW(),
        'ADMIN',
        1
    ),
    (
        2,
        paseape_db.uuid_v4 (),
        '60 MINUTOS',
        60,
        1,
        NOW(),
        'ADMIN',
        1
    ),
    (
        3,
        paseape_db.uuid_v4 (),
        '90 MINUTOS',
        90,
        2,
        NOW(),
        'ADMIN',
        1
    ),
    (
        4,
        paseape_db.uuid_v4 (),
        '120 MINUTOS',
        120,
        2,
        NOW(),
        'ADMIN',
        1
    );

INSERT INTO
    `paseape_db`.`tipo_modalidad_paseo` (
        `id`,
        `uuid`,
        `descripcion`,
        `detalle`,
        `creado_el`,
        `creado_por`,
        `estado`
    )
VALUES (
        1,
        paseape_db.uuid_v4 (),
        'PROGRAMADO',
        'Paseo reservado para una fecha y bloque horario futuro',
        NOW(),
        'ADMIN',
        1
    ),
    (
        2,
        paseape_db.uuid_v4 (),
        'INMEDIATO',
        'Paseo solicitado bajo demanda para atención inmediata',
        NOW(),
        'ADMIN',
        1
    );

INSERT INTO
    `paseape_db`.`solicitud_paseo_estado` (
        `id`,
        `uuid`,
        `descripcion`,
        `detalle`,
        `creado_el`,
        `creado_por`,
        `estado`
    )
VALUES (
        1,
        paseape_db.uuid_v4 (),
        'PENDIENTE',
        'Solicitud programada enviada al paseador a la espera de confirmación',
        NOW(),
        'ADMIN',
        1
    ),
    (
        2,
        paseape_db.uuid_v4 (),
        'ACEPTADA',
        'Paseo aceptado y confirmado por el paseador',
        NOW(),
        'ADMIN',
        1
    ),
    (
        3,
        paseape_db.uuid_v4 (),
        'RECHAZADA',
        'Solicitud declinada por el paseador',
        NOW(),
        'ADMIN',
        1
    ),
    (
        4,
        paseape_db.uuid_v4 (),
        'CANCELADA',
        'Paseo cancelado por el cliente antes de su inicio',
        NOW(),
        'ADMIN',
        1
    ),
    (
        5,
        paseape_db.uuid_v4 (),
        'EN_CURSO',
        'Paseo actualmente en progreso',
        NOW(),
        'ADMIN',
        1
    ),
    (
        6,
        paseape_db.uuid_v4 (),
        'FINALIZADA',
        'Paseo concluido satisfactoriamente',
        NOW(),
        'ADMIN',
        1
    ),
    (
        7,
        paseape_db.uuid_v4 (),
        'VENCIDA',
        'Solicitud expirada sin respuesta del paseador',
        NOW(),
        'ADMIN',
        1
    ),
    (
        8,
        paseape_db.uuid_v4 (),
        'SOLICITADA_BUSCANDO',
        'Solicitud inmediata abierta buscando paseador disponible en la zona',
        NOW(),
        'ADMIN',
        1
    );

INSERT INTO
    `paseape_db`.`tipo_notificacion` (
        `id`,
        `uuid`,
        `descripcion`,
        `detalle`,
        `plantilla_asunto`,
        `creado_el`,
        `creado_por`,
        `estado`
    )
VALUES (
        1,
        paseape_db.uuid_v4 (),
        'SOLICITUD_RECIBIDA',
        'Notificación al paseador al crearse una solicitud',
        '¡Nueva solicitud de paseo recibida!',
        NOW(),
        'ADMIN',
        1
    ),
    (
        2,
        paseape_db.uuid_v4 (),
        'SOLICITUD_ACEPTADA',
        'Notificación al cliente cuando el paseo es confirmado',
        '¡Tu paseo ha sido confirmado!',
        NOW(),
        'ADMIN',
        1
    ),
    (
        3,
        paseape_db.uuid_v4 (),
        'SOLICITUD_RECHAZADA',
        'Notificación al cliente cuando el paseo es declinado',
        'Actualización de tu solicitud de paseo',
        NOW(),
        'ADMIN',
        1
    ),
    (
        4,
        paseape_db.uuid_v4 (),
        'PASEO_INICIADO',
        'Notificación al cliente de inicio de trayecto',
        '¡El paseo de tu mascota ha comenzado!',
        NOW(),
        'ADMIN',
        1
    ),
    (
        5,
        paseape_db.uuid_v4 (),
        'PASEO_FINALIZADO',
        'Notificación al cliente de fin del recorrido',
        'El paseo ha finalizado con éxito',
        NOW(),
        'ADMIN',
        1
    ),
    (
        6,
        paseape_db.uuid_v4 (),
        'SOLICITUD_CANCELADA',
        'Aviso de cancelación previa de reserva',
        'Solicitud de paseo cancelada',
        NOW(),
        'ADMIN',
        1
    );