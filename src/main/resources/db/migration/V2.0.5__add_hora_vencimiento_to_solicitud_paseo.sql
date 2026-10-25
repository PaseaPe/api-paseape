-- =============================================================================
-- PROYECTO TECNOLÓGICO: PaseaPe (Lima 2026)
-- SPRINT 2: MIGRACIÓN V2.0.5 (CAMPO HORA_VENCIMIENTO PARA SOLICITUDES INMEDIATAS)
-- Motor: MySQL 8 | Base de datos: paseape_db
-- =============================================================================

ALTER TABLE `paseape_db`.`solicitud_paseo`
ADD COLUMN `hora_vencimiento` DATETIME NULL AFTER `motivo_rechazo_cancelacion`,
ADD INDEX `idx_sol_hora_vencimiento` (`hora_vencimiento` ASC);
