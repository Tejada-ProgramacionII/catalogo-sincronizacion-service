package ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.entity.HorarioSemanalEntity;

class HorarioSemanalMapperTest {

    private static final Instant CREADO_EN = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant ACTUALIZADO_EN = Instant.parse("2026-09-10T13:40:00Z");

    private final HorarioSemanalMapper mapper = new HorarioSemanalMapper();

    @Test
    void toEntityCopiaLosCamposYGuardaElDiaComoTexto() {
        HorarioSemanal horario = HorarioSemanal.builder()
                .id(21L)
                .profesionalId(15L)
                .diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(12, 0))
                .duracionTurnoMinutos(30)
                .habilitado(true)
                .creadoEn(CREADO_EN)
                .actualizadoEn(ACTUALIZADO_EN)
                .build();

        HorarioSemanalEntity entity = mapper.toEntity(horario);

        assertThat(entity).usingRecursiveComparison().ignoringFields("diaSemana").isEqualTo(horario);
        assertThat(entity.getDiaSemana()).isEqualTo("MONDAY");
    }

    @Test
    void toDomainModelCopiaLosCamposYConvierteElDia() {
        HorarioSemanalEntity entity = HorarioSemanalEntity.builder()
                .id(22L)
                .profesionalId(16L)
                .diaSemana("WEDNESDAY")
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(18, 0))
                .duracionTurnoMinutos(20)
                .habilitado(false)
                .creadoEn(CREADO_EN)
                .actualizadoEn(ACTUALIZADO_EN)
                .build();

        HorarioSemanal horario = mapper.toDomainModel(entity);

        assertThat(horario).usingRecursiveComparison().ignoringFields("diaSemana").isEqualTo(entity);
        assertThat(horario.getDiaSemana()).isEqualTo(DayOfWeek.WEDNESDAY);
    }

    @Test
    void toEntityDevuelveNullSiRecibeNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDomainModelDevuelveNullSiRecibeNull() {
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
