-- V4: tabla local de horarios semanales de atención (copia del catálogo de la cátedra).
--
-- El id es el que asigna la cátedra, por eso no se genera acá.
-- Cada horario pertenece a un profesional: la clave foránea asegura que
-- profesional_id exista en profesionales.
-- dia_semana guarda el nombre del día en inglés (MONDAY ... SUNDAY), como lo envía la cátedra.
-- Las bajas son lógicas: un horario deshabilitado queda con habilitado = false.
CREATE TABLE horarios_semanales (
    id                     BIGINT                   PRIMARY KEY,
    profesional_id         BIGINT                   NOT NULL,
    dia_semana             VARCHAR(9)               NOT NULL,
    hora_inicio            TIME                     NOT NULL,
    hora_fin               TIME                     NOT NULL,
    duracion_turno_minutos INTEGER                  NOT NULL,
    habilitado             BOOLEAN                  NOT NULL,
    creado_en              TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en         TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_horarios_semanales_profesional FOREIGN KEY (profesional_id) REFERENCES profesionales (id)
);
