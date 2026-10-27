package ar.edu.um.programacion2.catalogo.profesional.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

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
public class FiltroBusquedaProfesionales {
    private Long categoriaId;
    private String nombre;
    private Boolean habilitado;
    private LocalDate fecha;

    // D-09: la disponibilidad se mira por el día de la semana de la fecha pedida.
    public DayOfWeek getDiaSemana() {
        return fecha == null ? null : fecha.getDayOfWeek();
    }
}
