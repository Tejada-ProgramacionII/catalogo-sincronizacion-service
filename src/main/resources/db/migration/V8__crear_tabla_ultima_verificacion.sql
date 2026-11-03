-- V8: cuándo se verificó por última vez que la copia local está al día (D-07, CU-06).
--
-- Una verificación sin versiones pendientes no registra un intento de sincronización:
-- solo pisa esta fila. Como version_aplicada, tiene como máximo una fila (id = 1).
CREATE TABLE ultima_verificacion (
    id            BIGINT                   PRIMARY KEY,
    verificada_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_ultima_verificacion_fila_unica CHECK (id = 1)
);
