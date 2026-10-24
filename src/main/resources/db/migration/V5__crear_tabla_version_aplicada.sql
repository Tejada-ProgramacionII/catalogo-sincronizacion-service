-- V5: versión del catálogo de la cátedra aplicada localmente.
--
-- La tabla tiene como máximo una fila: el CHECK obliga a que el id valga 1
-- y la clave primaria impide repetirlo.
-- Sin filas significa "sin datos": todavía no hubo ninguna sincronización exitosa.
CREATE TABLE version_aplicada (
    id          BIGINT                   PRIMARY KEY,
    version     BIGINT                   NOT NULL,
    aplicada_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_version_aplicada_fila_unica CHECK (id = 1)
);
