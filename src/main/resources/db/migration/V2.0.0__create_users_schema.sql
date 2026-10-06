-- =============================================================================
-- PROYECTO TECNOLÓGICO: PaseaPe (Lima 2026)
-- SPRINT 1: MIGRACIÓN V2.0.0 (USUARIOS, SUBTIPOS, MASCOTAS Y LOOKUP TABLES)
-- Motor: MySQL 8 (Aiven Cloud Managed / XAMPP Local) | Base de datos: paseape_db
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. LOOKUP TABLES (TABLAS CATALOGO SIN DEPENDENCIAS)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_usuario`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(30)  NOT NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_usuario_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_usuario_unq` (`descripcion` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`usuario_estado`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(30)  NOT NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_usuario_estado_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_usuario_estado_unq` (`descripcion` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_proveedor_auth`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(30)  NOT NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_prov_auth_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_prov_auth_unq` (`descripcion` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_documento`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(50)  NOT NULL,
    `longitud_exacta`    INT          NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_documento_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_doc_unq` (`descripcion` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`paseador_estado_verificacion`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(50)  NOT NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_paseador_est_verif_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_paseador_est_verif_unq` (`descripcion` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`distritos_lima`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(100) NOT NULL,
    `codigo_ubigeo`      VARCHAR(10)  NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_distritos_lima_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_distrito_unq` (`descripcion` ASC),
    INDEX `idx_distritos_codigo_ubigeo` (`codigo_ubigeo` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_mascota`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(50)  NOT NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_mascota_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_mascota_unq` (`descripcion` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_tamaño_mascota`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(50)  NOT NULL,
    `rango_peso_ref`     VARCHAR(50)  NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_tamano_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_tamano_unq` (`descripcion` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_nivel_energia`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(50)  NOT NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_nivel_energia_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_energia_unq` (`descripcion` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_genero_mascota`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `descripcion`        VARCHAR(30)  NOT NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_genero_unq` (`uuid` ASC),
    UNIQUE INDEX `descripcion_tipo_genero_unq` (`descripcion` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. LOOKUP CON DEPENDENCIA (tipo_raza DEPENDE DE tipo_mascota)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `paseape_db`.`tipo_raza`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `tipo_mascota_id`    INT          NOT NULL,
    `descripcion`        VARCHAR(80)  NOT NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_tipo_raza_unq` (`uuid` ASC),
    INDEX `idx_tipo_raza_tipo_mascota` (`tipo_mascota_id` ASC),
    CONSTRAINT `fk_tipo_raza_tipo_mascota`
    FOREIGN KEY (`tipo_mascota_id`)
    REFERENCES `paseape_db`.`tipo_mascota` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. TABLA BASE: USUARIOS
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `paseape_db`.`usuarios`
(
    `id`                      BIGINT       NOT NULL AUTO_INCREMENT,
    `uuid`                    CHAR(36)     NOT NULL,
    `nombres`                 VARCHAR(100) NOT NULL,
    `apellidos`               VARCHAR(100) NOT NULL,
    `correo`                  VARCHAR(150) NOT NULL,
    `correo_verificado`       TINYINT      NOT NULL DEFAULT 0,
    `contrasena_hash`         VARCHAR(255) NULL,
    `telefono`                VARCHAR(15)  NULL,
    `foto_perfil_url`         VARCHAR(255) NULL,
    `tipo_usuario_id`         INT          NOT NULL,
    `usuario_estado_id`       INT          NOT NULL,
    `tipo_proveedor_auth_id`  INT          NOT NULL,
    `provider_id`             VARCHAR(100) NULL,
    `creado_el`               DATETIME     NOT NULL,
    `actualizado_el`          DATETIME     NULL,
    `creado_por`              VARCHAR(100) NOT NULL,
    `actualizado_por`         VARCHAR(100) NULL,
    `estado`                  BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_usuario_unq` (`uuid` ASC),
    UNIQUE INDEX `correo_usuario_unq` (`correo` ASC),
    UNIQUE INDEX `provider_id_usuario_unq` (`provider_id` ASC),
    INDEX `idx_usuarios_tipo_usuario` (`tipo_usuario_id` ASC),
    INDEX `idx_usuarios_usuario_estado` (`usuario_estado_id` ASC),
    INDEX `idx_usuarios_tipo_prov_auth` (`tipo_proveedor_auth_id` ASC),
    CONSTRAINT `fk_usuarios_tipo_usuario`
    FOREIGN KEY (`tipo_usuario_id`)
    REFERENCES `paseape_db`.`tipo_usuario` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_usuarios_usuario_estado`
    FOREIGN KEY (`usuario_estado_id`)
    REFERENCES `paseape_db`.`usuario_estado` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_usuarios_tipo_proveedor_auth`
    FOREIGN KEY (`tipo_proveedor_auth_id`)
    REFERENCES `paseape_db`.`tipo_proveedor_auth` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. TABLAS DE EXTENSIÓN / SUBTIPOS (1 A 1 VÍA SHARED PRIMARY KEY)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `paseape_db`.`clientes`
(
    `id`                            BIGINT       NOT NULL AUTO_INCREMENT,
    `uuid`                          CHAR(36)     NOT NULL,
    `direccion_referencia`          VARCHAR(255) NULL,
    `distrito_id`                   INT          NULL,
    `contacto_emergencia_nombre`    VARCHAR(100) NULL,
    `contacto_emergencia_telefono`  VARCHAR(15)  NULL,
    `notas_adicionales`             TEXT         NULL,
    `creado_el`                     DATETIME     NOT NULL,
    `actualizado_el`                DATETIME     NULL,
    `creado_por`                    VARCHAR(100) NOT NULL,
    `actualizado_por`               VARCHAR(100) NULL,
    `estado`                        BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_cliente_unq` (`uuid` ASC),
    INDEX `idx_clientes_distrito` (`distrito_id` ASC),
    CONSTRAINT `fk_clientes_usuarios`
    FOREIGN KEY (`id`)
    REFERENCES `paseape_db`.`usuarios` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_clientes_distritos_lima`
    FOREIGN KEY (`distrito_id`)
    REFERENCES `paseape_db`.`distritos_lima` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`paseadores`
(
    `id`                              BIGINT        NOT NULL AUTO_INCREMENT,
    `uuid`                            CHAR(36)      NOT NULL,
    `tipo_documento_id`               INT           NULL,
    `numero_documento`                VARCHAR(20)   NULL,
    `antecedentes_policiales_url`     VARCHAR(255)  NULL,
    `experiencia_años`                INT           NULL DEFAULT 0,
    `biografia`                       TEXT          NULL,
    `tarifa_hora_pen`                 DECIMAL(10,2) NULL,
    `distrito_cobertura_id`           INT           NULL,
    `paseador_estado_verificacion_id` INT           NOT NULL,
    `paseos_completados`              INT           NOT NULL DEFAULT 0,
    `calificacion_promedio`           DECIMAL(3,2)  NOT NULL DEFAULT 0.00,
    `creado_el`                       DATETIME      NOT NULL,
    `actualizado_el`                  DATETIME      NULL,
    `creado_por`                      VARCHAR(100)  NOT NULL,
    `actualizado_por`                 VARCHAR(100)  NULL,
    `estado`                          BIT           NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_paseador_unq` (`uuid` ASC),
    UNIQUE INDEX `numero_documento_paseador_unq` (`numero_documento` ASC),
    INDEX `idx_paseadores_tipo_documento` (`tipo_documento_id` ASC),
    INDEX `idx_paseadores_distrito_cobertura` (`distrito_cobertura_id` ASC),
    INDEX `idx_paseadores_estado_verificacion` (`paseador_estado_verificacion_id` ASC),
    CONSTRAINT `fk_paseadores_usuarios`
    FOREIGN KEY (`id`)
    REFERENCES `paseape_db`.`usuarios` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_paseadores_tipo_documento`
    FOREIGN KEY (`tipo_documento_id`)
    REFERENCES `paseape_db`.`tipo_documento` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_paseadores_distritos_lima`
    FOREIGN KEY (`distrito_cobertura_id`)
    REFERENCES `paseape_db`.`distritos_lima` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_paseadores_estado_verificacion`
    FOREIGN KEY (`paseador_estado_verificacion_id`)
    REFERENCES `paseape_db`.`paseador_estado_verificacion` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `paseape_db`.`administradores`
(
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `codigo_empleado`    VARCHAR(50)  NOT NULL,
    `area_departamento`  VARCHAR(100) NULL,
    `superadmin`         BIT          NOT NULL DEFAULT 0,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_el`     DATETIME     NULL,
    `creado_por`         VARCHAR(100) NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `estado`             BIT          NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_administrador_unq` (`uuid` ASC),
    UNIQUE INDEX `codigo_empleado_admin_unq` (`codigo_empleado` ASC),
    CONSTRAINT `fk_administradores_usuarios`
    FOREIGN KEY (`id`)
    REFERENCES `paseape_db`.`usuarios` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. ENTIDAD TRANSACCIONAL: MASCOTAS
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `paseape_db`.`mascotas`
(
    `id`                      BIGINT        NOT NULL AUTO_INCREMENT,
    `uuid`                    CHAR(36)      NOT NULL,
    `cliente_id`              BIGINT        NOT NULL,
    `nombre`                  VARCHAR(60)   NOT NULL,
    `tipo_mascota_id`         INT           NOT NULL,
    `tipo_raza_id`            INT           NOT NULL,
    `tipo_genero_mascota_id`  INT           NOT NULL,
    `tipo_tamaño_mascota_id`  INT           NOT NULL,
    `tipo_nivel_energia_id`   INT           NOT NULL,
    `edad_años`               INT           NULL DEFAULT 0,
    `edad_meses`              INT           NULL DEFAULT 0,
    `peso_kg`                 DECIMAL(5,2)  NULL,
    `esterilizado`            BIT           NOT NULL DEFAULT 0,
    `sociable_con_perros`     BIT           NOT NULL DEFAULT 1,
    `sociable_con_personas`   BIT           NOT NULL DEFAULT 1,
    `precauciones_medicas`    TEXT          NULL,
    `foto_url`                VARCHAR(255)  NULL,
    `creado_el`               DATETIME      NOT NULL,
    `actualizado_el`          DATETIME      NULL,
    `creado_por`              VARCHAR(100)  NOT NULL,
    `actualizado_por`         VARCHAR(100)  NULL,
    `estado`                  BIT           NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_mascota_unq` (`uuid` ASC),
    INDEX `idx_mascotas_cliente` (`cliente_id` ASC),
    INDEX `idx_mascotas_tipo_mascota` (`tipo_mascota_id` ASC),
    INDEX `idx_mascotas_tipo_raza` (`tipo_raza_id` ASC),
    INDEX `idx_mascotas_tipo_genero` (`tipo_genero_mascota_id` ASC),
    INDEX `idx_mascotas_tipo_tamano` (`tipo_tamaño_mascota_id` ASC),
    INDEX `idx_mascotas_tipo_nivel_energia` (`tipo_nivel_energia_id` ASC),
    CONSTRAINT `fk_mascotas_clientes`
    FOREIGN KEY (`cliente_id`)
    REFERENCES `paseape_db`.`clientes` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_mascotas_tipo_mascota`
    FOREIGN KEY (`tipo_mascota_id`)
    REFERENCES `paseape_db`.`tipo_mascota` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_mascotas_tipo_raza`
    FOREIGN KEY (`tipo_raza_id`)
    REFERENCES `paseape_db`.`tipo_raza` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_mascotas_tipo_genero`
    FOREIGN KEY (`tipo_genero_mascota_id`)
    REFERENCES `paseape_db`.`tipo_genero_mascota` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_mascotas_tipo_tamano`
    FOREIGN KEY (`tipo_tamaño_mascota_id`)
    REFERENCES `paseape_db`.`tipo_tamaño_mascota` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_mascotas_tipo_nivel_energia`
    FOREIGN KEY (`tipo_nivel_energia_id`)
    REFERENCES `paseape_db`.`tipo_nivel_energia` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;