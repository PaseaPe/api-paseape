-- =============================================================================
-- PROYECTO TECNOLÓGICO: PaseaPe (Lima 2026)
-- SPRINT 1: MIGRACIÓN V2.0.2 (AUDITORÍA DE CORREOS TRANSACCIONALES BREVO)
-- Motor: MySQL 8 (Aiven Cloud Managed / XAMPP Local) | Base de datos: paseape_db
-- =============================================================================

CREATE TABLE IF NOT EXISTS `paseape_db`.`brevo_email` (
                                                          `id`                 BIGINT        NOT NULL AUTO_INCREMENT,
                                                          `uuid`               CHAR(36)      NOT NULL,
    `usuario_id`         BIGINT        NULL,
    `destinatario_email` VARCHAR(150)  NOT NULL,
    `destinatario_nombre`VARCHAR(100)  NULL,
    `remitente_email`    VARCHAR(150)  NOT NULL,
    `asunto`             VARCHAR(200)  NOT NULL,
    `tipo_notificacion`  VARCHAR(50)   NOT NULL,
    `payload_enviado`    JSON          NULL,
    `respuesta_servicio` TEXT          NULL,
    `codigo_http`        INT           NULL,
    `brevo_message_id`   VARCHAR(100)  NULL,
    `exitoso`            TINYINT       NOT NULL DEFAULT 0,
    `error_mensaje`      TEXT          NULL,
    `creado_el`          DATETIME      NOT NULL,
    `actualizado_el`     DATETIME      NULL,
    `creado_por`         VARCHAR(100)  NOT NULL,
    `actualizado_por`    VARCHAR(100)  NULL,
    `estado`             BIT           NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_brevo_email_unq` (`uuid` ASC),
    INDEX `idx_brevo_email_usuario` (`usuario_id` ASC),
    INDEX `idx_brevo_email_destinatario` (`destinatario_email` ASC),
    INDEX `idx_brevo_email_tipo` (`tipo_notificacion` ASC),
    INDEX `idx_brevo_email_exitoso` (`exitoso` ASC),
    CONSTRAINT `fk_brevo_email_usuarios`
    FOREIGN KEY (`usuario_id`)
    REFERENCES `paseape_db`.`usuarios` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;