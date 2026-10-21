package ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.entity;

import java.time.Instant;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "horarios_semanales")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioSemanalEntity {
    @Id
    private Long id;
    private Long profesionalId;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int duracionTurnoMinutos;
    private boolean habilitado;
    private Instant creadoEn;
    private Instant actualizadoEn;
}
