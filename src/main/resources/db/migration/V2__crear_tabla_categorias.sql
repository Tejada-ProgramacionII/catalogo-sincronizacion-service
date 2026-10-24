-- V2: tabla local de categorías profesionales (copia del catálogo de la cátedra).
--
-- El id es el que asigna la cátedra, por eso no se genera acá.
-- Las bajas son lógicas: una categoría deshabilitada queda con habilitada = false.
CREATE TABLE categorias (
    id             BIGINT                   PRIMARY KEY,
    nombre         VARCHAR(255)             NOT NULL,
    descripcion    VARCHAR(500),
    habilitada     BOOLEAN                  NOT NULL,
    creada_en      TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizada_en TIMESTAMP WITH TIME ZONE NOT NULL
);
