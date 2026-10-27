package ar.edu.um.programacion2.catalogo.profesional.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class FiltroBusquedaProfesionalesTest {

    @Test
    void getDiaSemanaDevuelveElDiaDeLaFechaPedida() {
        FiltroBusquedaProfesionales filtro = FiltroBusquedaProfesionales.builder()
                .fecha(LocalDate.of(2026, 9, 14))
                .build();

        assertThat(filtro.getDiaSemana()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void getDiaSemanaDevuelveNullSiNoSeFiltraPorFecha() {
        assertThat(new FiltroBusquedaProfesionales().getDiaSemana()).isNull();
    }
}
