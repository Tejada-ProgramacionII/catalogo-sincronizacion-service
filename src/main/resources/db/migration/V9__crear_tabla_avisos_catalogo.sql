-- V9: avisos CatalogUpdated de Kafka ya procesados (CU-02, paso 1; contrato 15.1 y 18.1).
--
-- Kafka entrega al menos una vez: el event_id es la clave de idempotencia y, con esta
-- tabla, un aviso repetido se reconoce aunque el servicio se haya reiniciado.
CREATE TABLE avisos_catalogo (
    event_id     VARCHAR(100)             PRIMARY KEY,
    version      BIGINT                   NOT NULL,
    procesado_en TIMESTAMP WITH TIME ZONE NOT NULL
);
