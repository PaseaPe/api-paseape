-- =============================================================================
-- PROYECTO TECNOLÓGICO: PaseaPe (Lima 2026) - NRC 30633
-- SPRINT 1 (AV2): MIGRACIÓN BASE V0 (INFRAESTRUCTURA, UUIDv4 Y KEEPALIVE)
-- Motor: MySQL 8 (Aiven Cloud Managed / XAMPP Local) | Base de datos: paseape_db
-- Reglas de Ingeniería:
--   1. ID autoincremental estricto.
--   2. Inserción directa (INSERT VALUES).
--   3. Cero UDFs en cláusula DEFAULT (resuelto vía BEFORE INSERT trigger).
--   4. Cero DEFAULT CURRENT_TIMESTAMP en auditoría (gestionado por JPA).
-- =============================================================================

CREATE FUNCTION paseape_db.uuid_v4()
    RETURNS CHAR(36)
    NO SQL
BEGIN
RETURN LOWER(CONCAT(
        LPAD(HEX(FLOOR(RAND() * 0xffffffff)), 8, '0'), '-',
        LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0'), '-',
        '4',
        LPAD(HEX(FLOOR(RAND() * 0xfff)), 3, '0'), '-',
        HEX(FLOOR(RAND() * 4) + 8),
        LPAD(HEX(FLOOR(RAND() * 0xfff)), 3, '0'), '-',
        LPAD(HEX(FLOOR(RAND() * 0xffffffff)), 8, '0'),
        LPAD(HEX(FLOOR(RAND() * 0xffff)), 4, '0')
             ));
END;

-- -----------------------------------------------------------------------------
-- 2. TABLA DE INFRAESTRUCTURA: SISTEMA HEARTBEAT
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `paseape_db`.`sistema_heartbeat`
(
    `id`                 INT          NOT NULL AUTO_INCREMENT,
    `uuid`               CHAR(36)     NOT NULL,
    `componente`         VARCHAR(50)  NOT NULL,
    `ultimo_latido`      DATETIME     NOT NULL,
    `latidos_acumulados` BIGINT       NOT NULL DEFAULT 1,
    `ip_origen`          VARCHAR(45)  NULL,
    `descripcion`        VARCHAR(150) NULL,
    `estado`             INT          NOT NULL DEFAULT 1,
    `creado_por`         VARCHAR(100) NOT NULL,
    `creado_el`          DATETIME     NOT NULL,
    `actualizado_por`    VARCHAR(100) NULL,
    `actualizado_el`     DATETIME     NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uuid_heartbeat_unq` (`uuid` ASC),
    UNIQUE INDEX `componente_heartbeat_unq` (`componente` ASC)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. REGISTRO SEMILLA SIMPLE (CAMPOS DE ACTUALIZACIÓN NULOS EN CREACIÓN)
-- -----------------------------------------------------------------------------
INSERT INTO `paseape_db`.`sistema_heartbeat`( `uuid`,`componente`, `ultimo_latido`, `latidos_acumulados`, `ip_origen`, `descripcion`, `estado`,
                                             `creado_por`, `creado_el`)
VALUES
    ( paseape_db.uuid_v4(),'BACKEND_OCI_PASEAPE', NOW(), 1, '157.151.203.121',
     'Heartbeat preventivo Aiven MySQL - Lima 2026',  1,  'ADMIN', NOW());