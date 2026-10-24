package ar.edu.um.programacion2.catalogo.horario.domain.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioSemanal {
    private Long id;
    private Long profesionalId;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int duracionTurnoMinutos;
    private boolean habilitado;
    private Instant creadoEn;
    private Instant actualizadoEn;
}
