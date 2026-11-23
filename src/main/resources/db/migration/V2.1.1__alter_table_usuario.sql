ALTER TABLE `paseape_db`.`usuarios`
    ADD COLUMN `folder_digital_document_id` BIGINT NULL AFTER `provider_id`,
    ADD CONSTRAINT `fk_usuarios_folder_document`
        FOREIGN KEY (`folder_digital_document_id`)
        REFERENCES `paseape_db`.`folder_digital_document` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE;