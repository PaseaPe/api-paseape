-- =============================================================================
-- PROYECTO TECNOLÓGICO: PaseaPe (Lima 2026)
-- SPRINT 1: MIGRACIÓN V2.0.3 (GESTIÓN DE DOCUMENTOS DIGITALES Y REFACTORIZACIÓN MASCOTAS)
-- Motor: MySQL 8 (Aiven Cloud Managed / XAMPP Local) | Base de datos: paseape_db
-- =============================================================================

CREATE TABLE IF NOT EXISTS `paseape_db`.`folder_digital_document` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `uuid`            CHAR(36)     NOT NULL,
    `nombre`          VARCHAR(100) NOT NULL,
    `creado_el`       DATETIME     NOT NULL,
    `actualizado_el`  DATETIME     NULL,
    `creado_por`      VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado`          BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_folder_digital_document_unq` (`uuid` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`digital_document_type` (
    `id`              INT          NOT NULL AUTO_INCREMENT,
    `uuid`            CHAR(36)     NOT NULL,
    `descripcion`     VARCHAR(50)  NOT NULL,
    `creado_el`       DATETIME     NOT NULL,
    `actualizado_el`  DATETIME     NULL,
    `creado_por`      VARCHAR(100) NOT NULL,
    `actualizado_por` VARCHAR(100) NULL,
    `estado`          BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_digital_document_type_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_digital_document_type_unq` (`descripcion` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

INSERT INTO `paseape_db`.`digital_document_type` (`id`, `uuid`, `descripcion`, `creado_el`, `creado_por`, `estado`) VALUES
        (1, paseape_db.uuid_v4(), 'FOTO_PERFIL', NOW(), 'ADMIN', 1),
        (2, paseape_db.uuid_v4(), 'FOTO_MASCOTA', NOW(), 'ADMIN', 1),
        (3, paseape_db.uuid_v4(), 'ANTECEDENTE_POLICIAL', NOW(), 'ADMIN', 1),
        (4, paseape_db.uuid_v4(), 'DOCUMENTO_IDENTIDAD', NOW(), 'ADMIN', 1);

CREATE TABLE IF NOT EXISTS `paseape_db`.`digital_document` (
    `id`                         BIGINT        NOT NULL AUTO_INCREMENT,
    `uuid`                       CHAR(36)      NOT NULL,
    `folder_digital_document_id` BIGINT        NOT NULL,
    `digital_document_type_id`   INT           NOT NULL,
    `object_name`                VARCHAR(255)  NOT NULL,
    `nombre_original`            VARCHAR(255)  NOT NULL,
    `content_type`               VARCHAR(100)  NOT NULL,
    `tamano_bytes`               BIGINT        NOT NULL,
    `creado_el`                  DATETIME      NOT NULL,
    `actualizado_el`             DATETIME      NULL,
    `creado_por`                 VARCHAR(100)  NOT NULL,
    `actualizado_por`            VARCHAR(100)  NULL,
    `estado`                     BIT           NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_digital_document_unq` (`uuid` ASC),
    INDEX `idx_digital_doc_folder` (`folder_digital_document_id` ASC),
    INDEX `idx_digital_doc_type` (`digital_document_type_id` ASC),
    CONSTRAINT `fk_digital_doc_folder`
    FOREIGN KEY (`folder_digital_document_id`)
    REFERENCES `paseape_db`.`folder_digital_document` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_digital_doc_type`
    FOREIGN KEY (`digital_document_type_id`)
    REFERENCES `paseape_db`.`digital_document_type` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `paseape_db`.`mascotas`
    ADD COLUMN `folder_digital_document_id` BIGINT NULL AFTER `precauciones_medicas`,
    ADD CONSTRAINT `fk_mascotas_folder_document`
        FOREIGN KEY (`folder_digital_document_id`)
        REFERENCES `paseape_db`.`folder_digital_document` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE;

ALTER TABLE `paseape_db`.`mascotas` DROP COLUMN `foto_url`;