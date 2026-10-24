-- V3: tabla local de profesionales (copia del catálogo de la cátedra).
--
-- El id es el que asigna la cátedra, por eso no se genera acá.
-- Cada profesional pertenece a una categoría: la clave foránea asegura que
-- categoria_id exista en categorias.
-- Las bajas son lógicas: un profesional deshabilitado queda con habilitado = false.
CREATE TABLE profesionales (
    id             BIGINT                   PRIMARY KEY,
    categoria_id   BIGINT                   NOT NULL,
    nombre         VARCHAR(255)             NOT NULL,
    apellido       VARCHAR(255)             NOT NULL,
    habilitado     BOOLEAN                  NOT NULL,
    creado_en      TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_profesionales_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id)
);
