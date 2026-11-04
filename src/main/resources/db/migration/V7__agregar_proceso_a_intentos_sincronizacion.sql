-- V7: proceso de cada sincronización (CU-06) y el resultado DISCONTINUIDAD (CU-03).
--
-- Las filas que ya existen son todas de la sincronización completa: el DEFAULT solo
-- sirve para completarlas y se saca después, así cada fila nueva tiene que decir su proceso.
ALTER TABLE intentos_sincronizacion ADD COLUMN proceso VARCHAR(15) DEFAULT 'COMPLETA' NOT NULL;
ALTER TABLE intentos_sincronizacion ALTER COLUMN proceso DROP DEFAULT;
ALTER TABLE intentos_sincronizacion ADD CONSTRAINT ck_intentos_sincronizacion_proceso
    CHECK (proceso IN ('COMPLETA', 'INCREMENTAL', 'RECUPERACION'));

ALTER TABLE intentos_sincronizacion DROP CONSTRAINT ck_intentos_sincronizacion_resultado;
ALTER TABLE intentos_sincronizacion ADD CONSTRAINT ck_intentos_sincronizacion_resultado
    CHECK (resultado IN ('OK', 'CATEDRA_NO_DISPONIBLE', 'RECHAZO', 'DISCONTINUIDAD', 'ERROR'));
