INSERT INTO `paseape_db`.`tipo_usuario` (
    `id`,
    `uuid`,
    `descripcion`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      (1, paseape_db.uuid_v4(), 'CLIENTE',       NOW(), 'ADMIN', 1),
      (2, paseape_db.uuid_v4(), 'PASEADOR',      NOW(), 'ADMIN', 1),
      (3, paseape_db.uuid_v4(), 'ADMINISTRADOR', NOW(), 'ADMIN', 1);

INSERT INTO `paseape_db`.`usuario_estado` (
    `id`,
    `uuid`,
    `descripcion`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      (1, paseape_db.uuid_v4(), 'ACTIVO',               NOW(), 'ADMIN', 1),
      (2, paseape_db.uuid_v4(), 'INACTIVO',             NOW(), 'ADMIN', 1),
      (3, paseape_db.uuid_v4(), 'PENDIENTE_ACTIVACION', NOW(), 'ADMIN', 1),
      (4, paseape_db.uuid_v4(), 'SUSPENDIDO',           NOW(), 'ADMIN', 1),
      (5, paseape_db.uuid_v4(), 'BLOQUEADO',            NOW(), 'ADMIN', 1);

INSERT INTO `paseape_db`.`tipo_proveedor_auth` (
    `id`,
    `uuid`,
    `descripcion`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      (1, paseape_db.uuid_v4(), 'LOCAL',  NOW(), 'ADMIN', 1),
      (2, paseape_db.uuid_v4(), 'GOOGLE', NOW(), 'ADMIN', 1);

INSERT INTO `paseape_db`.`tipo_documento` (
    `id`,
    `uuid`,
    `descripcion`,
    `longitud_exacta`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      (1, paseape_db.uuid_v4(), 'DNI',               8,    NOW(), 'ADMIN', 1),
      (2, paseape_db.uuid_v4(), 'CARNET_EXTRANJERIA', NULL, NOW(), 'ADMIN', 1),
      (3, paseape_db.uuid_v4(), 'PASAPORTE',          NULL, NOW(), 'ADMIN', 1);

INSERT INTO `paseape_db`.`paseador_estado_verificacion` (
    `id`,
    `uuid`,
    `descripcion`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      (1, paseape_db.uuid_v4(), 'PENDIENTE', NOW(), 'ADMIN', 1),
      (2, paseape_db.uuid_v4(), 'EN_REVISION', NOW(), 'ADMIN', 1),
      (3, paseape_db.uuid_v4(), 'APROBADO', NOW(), 'ADMIN', 1),
      (4, paseape_db.uuid_v4(), 'RECHAZADO', NOW(), 'ADMIN', 1),
      (5, paseape_db.uuid_v4(), 'OBSERVADO', NOW(), 'ADMIN', 1);

INSERT INTO `paseape_db`.`distritos_lima` (
    `id`,
    `uuid`,
    `descripcion`,
    `codigo_ubigeo`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      (1,  paseape_db.uuid_v4(), 'LIMA',                   '150101', NOW(), 'ADMIN', 1),
      (2,  paseape_db.uuid_v4(), 'ANCON',                  '150102', NOW(), 'ADMIN', 1),
      (3,  paseape_db.uuid_v4(), 'ATE',                    '150103', NOW(), 'ADMIN', 1),
      (4,  paseape_db.uuid_v4(), 'BARRANCO',              '150104', NOW(), 'ADMIN', 1),
      (5,  paseape_db.uuid_v4(), 'BRENA',                  '150105', NOW(), 'ADMIN', 1),
      (6,  paseape_db.uuid_v4(), 'CARABAYLLO',             '150106', NOW(), 'ADMIN', 1),
      (7,  paseape_db.uuid_v4(), 'CHACLACAYO',             '150107', NOW(), 'ADMIN', 1),
      (8,  paseape_db.uuid_v4(), 'CHORRILLOS',             '150108', NOW(), 'ADMIN', 1),
      (9,  paseape_db.uuid_v4(), 'CIENEGUILLA',            '150109', NOW(), 'ADMIN', 1),
      (10, paseape_db.uuid_v4(), 'COMAS',                  '150110', NOW(), 'ADMIN', 1),
      (11, paseape_db.uuid_v4(), 'EL AGUSTINO',            '150111', NOW(), 'ADMIN', 1),
      (12, paseape_db.uuid_v4(), 'INDEPENDENCIA',          '150112', NOW(), 'ADMIN', 1),
      (13, paseape_db.uuid_v4(), 'JESUS MARIA',            '150113', NOW(), 'ADMIN', 1),
      (14, paseape_db.uuid_v4(), 'LA MOLINA',              '150114', NOW(), 'ADMIN', 1),
      (15, paseape_db.uuid_v4(), 'LA VICTORIA',            '150115', NOW(), 'ADMIN', 1),
      (16, paseape_db.uuid_v4(), 'LINCE',                  '150116', NOW(), 'ADMIN', 1),
      (17, paseape_db.uuid_v4(), 'LOS OLIVOS',             '150117', NOW(), 'ADMIN', 1),
      (18, paseape_db.uuid_v4(), 'LURIGANCHO',             '150118', NOW(), 'ADMIN', 1),
      (19, paseape_db.uuid_v4(), 'LURIN',                  '150119', NOW(), 'ADMIN', 1),
      (20, paseape_db.uuid_v4(), 'MAGDALENA DEL MAR',      '150120', NOW(), 'ADMIN', 1),
      (21, paseape_db.uuid_v4(), 'MIRAFLORES',             '150121', NOW(), 'ADMIN', 1),
      (22, paseape_db.uuid_v4(), 'PACHACAMAC',             '150122', NOW(), 'ADMIN', 1),
      (23, paseape_db.uuid_v4(), 'PUCUSANA',               '150123', NOW(), 'ADMIN', 1),
      (24, paseape_db.uuid_v4(), 'PUEBLO LIBRE',           '150124', NOW(), 'ADMIN', 1),
      (25, paseape_db.uuid_v4(), 'PUENTE PIEDRA',          '150125', NOW(), 'ADMIN', 1),
      (26, paseape_db.uuid_v4(), 'PUNTA HERMOSA',          '150126', NOW(), 'ADMIN', 1),
      (27, paseape_db.uuid_v4(), 'PUNTA NEGRA',            '150127', NOW(), 'ADMIN', 1),
      (28, paseape_db.uuid_v4(), 'RIMAC',                  '150128', NOW(), 'ADMIN', 1),
      (29, paseape_db.uuid_v4(), 'SAN BARTOLO',            '150129', NOW(), 'ADMIN', 1),
      (30, paseape_db.uuid_v4(), 'SAN BORJA',              '150130', NOW(), 'ADMIN', 1),
      (31, paseape_db.uuid_v4(), 'SAN ISIDRO',             '150131', NOW(), 'ADMIN', 1),
      (32, paseape_db.uuid_v4(), 'SAN JUAN DE LURIGANCHO', '150132', NOW(), 'ADMIN', 1),
      (33, paseape_db.uuid_v4(), 'SAN JUAN DE MIRAFLORES', '150133', NOW(), 'ADMIN', 1),
      (34, paseape_db.uuid_v4(), 'SAN LUIS',               '150134', NOW(), 'ADMIN', 1),
      (35, paseape_db.uuid_v4(), 'SAN MARTIN DE PORRES',   '150135', NOW(), 'ADMIN', 1),
      (36, paseape_db.uuid_v4(), 'SAN MIGUEL',             '150136', NOW(), 'ADMIN', 1),
      (37, paseape_db.uuid_v4(), 'SANTA ANITA',            '150137', NOW(), 'ADMIN', 1),
      (38, paseape_db.uuid_v4(), 'SANTA MARIA DEL MAR',    '150138', NOW(), 'ADMIN', 1),
      (39, paseape_db.uuid_v4(), 'SANTA ROSA',             '150139', NOW(), 'ADMIN', 1),
      (40, paseape_db.uuid_v4(), 'SANTIAGO DE SURCO',      '150140', NOW(), 'ADMIN', 1),
      (41, paseape_db.uuid_v4(), 'SURQUILLO',              '150141', NOW(), 'ADMIN', 1),
      (42, paseape_db.uuid_v4(), 'VILLA EL SALVADOR',      '150142', NOW(), 'ADMIN', 1),
      (43, paseape_db.uuid_v4(), 'VILLA MARIA DEL TRIUNFO','150143', NOW(), 'ADMIN', 1);

INSERT INTO `paseape_db`.`tipo_mascota` (
    `id`,
    `uuid`,
    `descripcion`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      (1, paseape_db.uuid_v4(), 'PERRO', NOW(), 'ADMIN', 1),
      (2, paseape_db.uuid_v4(), 'GATO',  NOW(), 'ADMIN', 1);

INSERT INTO `paseape_db`.`tipo_tamaño_mascota` (
    `id`,
    `uuid`,
    `descripcion`,
    `rango_peso_ref`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      (1, paseape_db.uuid_v4(), 'TOY',     'Hasta 5 kg',   NOW(), 'ADMIN', 1),
      (2, paseape_db.uuid_v4(), 'PEQUEÑO', '5 kg a 10 kg',  NOW(), 'ADMIN', 1),
      (3, paseape_db.uuid_v4(), 'MEDIANO', '10 kg a 25 kg', NOW(), 'ADMIN', 1),
      (4, paseape_db.uuid_v4(), 'GRANDE',  '25 kg a 45 kg', NOW(), 'ADMIN', 1),
      (5, paseape_db.uuid_v4(), 'GIGANTE', 'Más de 45 kg',  NOW(), 'ADMIN', 1);

INSERT INTO `paseape_db`.`tipo_nivel_energia` (
    `id`,
    `uuid`,
    `descripcion`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      (1, paseape_db.uuid_v4(), 'BAJO',      NOW(), 'ADMIN', 1),
      (2, paseape_db.uuid_v4(), 'MODERADO',  NOW(), 'ADMIN', 1),
      (3, paseape_db.uuid_v4(), 'ALTO',      NOW(), 'ADMIN', 1),
      (4, paseape_db.uuid_v4(), 'MUY_ALTO',  NOW(), 'ADMIN', 1);

INSERT INTO `paseape_db`.`tipo_genero_mascota` (
    `id`,
    `uuid`,
    `descripcion`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      (1, paseape_db.uuid_v4(), 'MACHO', NOW(), 'ADMIN', 1),
      (2, paseape_db.uuid_v4(), 'HEMBRA', NOW(), 'ADMIN', 1);

INSERT INTO `paseape_db`.`tipo_raza` (
    `id`,
    `uuid`,
    `tipo_mascota_id`,
    `descripcion`,
    `creado_el`,
    `creado_por`,
    `estado`
) VALUES
      -- -------------------------------------------------------------------------
      -- RAZAS DE PERROS (tipo_mascota_id = 1)
      -- -------------------------------------------------------------------------
      (1,  paseape_db.uuid_v4(), 1, 'MESTIZO / CRIOLLO',           NOW(), 'ADMIN', 1),
      (2,  paseape_db.uuid_v4(), 1, 'PERRO SIN PELO DEL PERU',     NOW(), 'ADMIN', 1),
      (3,  paseape_db.uuid_v4(), 1, 'LABRADOR RETRIEVER',          NOW(), 'ADMIN', 1),
      (4,  paseape_db.uuid_v4(), 1, 'GOLDEN RETRIEVER',            NOW(), 'ADMIN', 1),
      (5,  paseape_db.uuid_v4(), 1, 'PASTOR ALEMAN',               NOW(), 'ADMIN', 1),
      (6,  paseape_db.uuid_v4(), 1, 'BULLDOG FRANCES',             NOW(), 'ADMIN', 1),
      (7,  paseape_db.uuid_v4(), 1, 'BULLDOG INGLES',              NOW(), 'ADMIN', 1),
      (8,  paseape_db.uuid_v4(), 1, 'BEAGLE',                      NOW(), 'ADMIN', 1),
      (9,  paseape_db.uuid_v4(), 1, 'POODLE / CANICHE',            NOW(), 'ADMIN', 1),
      (10, paseape_db.uuid_v4(), 1, 'SCHNAUZER',                   NOW(), 'ADMIN', 1),
      (11, paseape_db.uuid_v4(), 1, 'PUG',                         NOW(), 'ADMIN', 1),
      (12, paseape_db.uuid_v4(), 1, 'BOXER',                       NOW(), 'ADMIN', 1),
      (13, paseape_db.uuid_v4(), 1, 'ROTTWEILER',                  NOW(), 'ADMIN', 1),
      (14, paseape_db.uuid_v4(), 1, 'SIBERIAN HUSKY',              NOW(), 'ADMIN', 1),
      (15, paseape_db.uuid_v4(), 1, 'BORDER COLLIE',               NOW(), 'ADMIN', 1),
      (16, paseape_db.uuid_v4(), 1, 'SHIH TZU',                    NOW(), 'ADMIN', 1),
      (17, paseape_db.uuid_v4(), 1, 'YORKSHIRE TERRIER',           NOW(), 'ADMIN', 1),
      (18, paseape_db.uuid_v4(), 1, 'CHIHUAHUA',                   NOW(), 'ADMIN', 1),
      (19, paseape_db.uuid_v4(), 1, 'DACHSHUND / TECKEL',          NOW(), 'ADMIN', 1),
      (20, paseape_db.uuid_v4(), 1, 'AMERICAN PIT BULL TERRIER',   NOW(), 'ADMIN', 1),
      (21, paseape_db.uuid_v4(), 1, 'AMERICAN BULLY',              NOW(), 'ADMIN', 1),
      (22, paseape_db.uuid_v4(), 1, 'COCKER SPANIEL',              NOW(), 'ADMIN', 1),
      (23, paseape_db.uuid_v4(), 1, 'DOBERMAN',                    NOW(), 'ADMIN', 1),
      (24, paseape_db.uuid_v4(), 1, 'SAMOYEDO',                    NOW(), 'ADMIN', 1),
      (25, paseape_db.uuid_v4(), 1, 'CHOW CHOW',                   NOW(), 'ADMIN', 1),
      (26, paseape_db.uuid_v4(), 1, 'JACK RUSSELL TERRIER',        NOW(), 'ADMIN', 1),
      (27, paseape_db.uuid_v4(), 1, 'BULL TERRIER',                NOW(), 'ADMIN', 1),
      (28, paseape_db.uuid_v4(), 1, 'DALMATA',                     NOW(), 'ADMIN', 1),
      (29, paseape_db.uuid_v4(), 1, 'OTRA RAZA CANINA',            NOW(), 'ADMIN', 1),

      -- -------------------------------------------------------------------------
      -- RAZAS DE GATOS (tipo_mascota_id = 2)
      -- -------------------------------------------------------------------------
      (30, paseape_db.uuid_v4(), 2, 'MESTIZO / COMUN EUROPEO',     NOW(), 'ADMIN', 1),
      (31, paseape_db.uuid_v4(), 2, 'SIAMES',                      NOW(), 'ADMIN', 1),
      (32, paseape_db.uuid_v4(), 2, 'PERSA',                       NOW(), 'ADMIN', 1),
      (33, paseape_db.uuid_v4(), 2, 'MAINE COON',                  NOW(), 'ADMIN', 1),
      (34, paseape_db.uuid_v4(), 2, 'BENGALA',                     NOW(), 'ADMIN', 1),
      (35, paseape_db.uuid_v4(), 2, 'ANGORA TURCO',                NOW(), 'ADMIN', 1),
      (36, paseape_db.uuid_v4(), 2, 'RAGDOLL',                     NOW(), 'ADMIN', 1),
      (37, paseape_db.uuid_v4(), 2, 'ESFINGE / SPHYNX',            NOW(), 'ADMIN', 1),
      (38, paseape_db.uuid_v4(), 2, 'BRITISH SHORTHAIR',           NOW(), 'ADMIN', 1),
      (39, paseape_db.uuid_v4(), 2, 'AZUL RUSO',                   NOW(), 'ADMIN', 1),
      (40, paseape_db.uuid_v4(), 2, 'OTRA RAZA FELINA',            NOW(), 'ADMIN', 1);