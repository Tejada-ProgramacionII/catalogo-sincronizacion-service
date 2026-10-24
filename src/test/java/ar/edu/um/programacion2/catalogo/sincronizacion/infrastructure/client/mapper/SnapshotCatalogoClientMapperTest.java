package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.SnapshotCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.CategoriaCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.HorarioSemanalCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.ProfesionalCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.SnapshotCatalogoResponse;

class SnapshotCatalogoClientMapperTest {

    private static final Instant GENERADO_EN = Instant.parse("2026-09-10T13:44:59Z");
    private static final Instant CREADO_EN = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant ACTUALIZADO_EN = Instant.parse("2026-09-10T13:40:00Z");

    private final SnapshotCatalogoClientMapper mapper = new SnapshotCatalogoClientMapper();

    @Test
    void toDomainModelTraduceElSnapshotCompleto() {
        SnapshotCatalogoResponse response = SnapshotCatalogoResponse.builder()
                .snapshotVersion(7L)
                .generatedAt(GENERADO_EN)
                .professionalCategories(List.of(CategoriaCatedraResponse.builder()
                        .id(1L)
                        .name("Clinica medica")
                        .description("Atencion clinica general")
                        .enabled(true)
                        .createdAt(CREADO_EN)
                        .updatedAt(ACTUALIZADO_EN)
                        .build()))
                .professionals(List.of(ProfesionalCatedraResponse.builder()
                        .id(15L)
                        .categoryId(1L)
                        .firstName("Ana")
                        .lastName("Gomez")
                        .enabled(false)
                        .createdAt(CREADO_EN)
                        .updatedAt(ACTUALIZADO_EN)
                        .build()))
                .weeklySchedules(List.of(HorarioSemanalCatedraResponse.builder()
                        .id(21L)
                        .professionalId(15L)
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(12, 0))
                        .slotDurationMinutes(30)
                        .enabled(true)
                        .createdAt(CREADO_EN)
                        .updatedAt(ACTUALIZADO_EN)
                        .build()))
                .build();

        SnapshotCatalogo snapshot = mapper.toDomainModel(response);

        assertThat(snapshot.getVersion()).isEqualTo(7L);
        assertThat(snapshot.getGeneradoEn()).isEqualTo(GENERADO_EN);

        assertThat(snapshot.getCategorias()).singleElement().satisfies(categoria -> {
            assertThat(categoria.getId()).isEqualTo(1L);
            assertThat(categoria.getNombre()).isEqualTo("Clinica medica");
            assertThat(categoria.getDescripcion()).isEqualTo("Atencion clinica general");
            assertThat(categoria.isHabilitada()).isTrue();
            assertThat(categoria.getCreadaEn()).isEqualTo(CREADO_EN);
            assertThat(categoria.getActualizadaEn()).isEqualTo(ACTUALIZADO_EN);
        });

        assertThat(snapshot.getProfesionales()).singleElement().satisfies(profesional -> {
            assertThat(profesional.getId()).isEqualTo(15L);
            assertThat(profesional.getCategoriaId()).isEqualTo(1L);
            assertThat(profesional.getNombre()).isEqualTo("Ana");
            assertThat(profesional.getApellido()).isEqualTo("Gomez");
            assertThat(profesional.isHabilitado()).isFalse();
            assertThat(profesional.getCreadoEn()).isEqualTo(CREADO_EN);
            assertThat(profesional.getActualizadoEn()).isEqualTo(ACTUALIZADO_EN);
        });

        assertThat(snapshot.getHorarios()).singleElement().satisfies(horario -> {
            assertThat(horario.getId()).isEqualTo(21L);
            assertThat(horario.getProfesionalId()).isEqualTo(15L);
            assertThat(horario.getDiaSemana()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(horario.getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
            assertThat(horario.getHoraFin()).isEqualTo(LocalTime.of(12, 0));
            assertThat(horario.getDuracionTurnoMinutos()).isEqualTo(30);
            assertThat(horario.isHabilitado()).isTrue();
            assertThat(horario.getCreadoEn()).isEqualTo(CREADO_EN);
            assertThat(horario.getActualizadoEn()).isEqualTo(ACTUALIZADO_EN);
        });
    }

    @Test
    void toDomainModelConListasVaciasDevuelveListasVacias() {
        SnapshotCatalogoResponse response = SnapshotCatalogoResponse.builder()
                .snapshotVersion(1L)
                .generatedAt(GENERADO_EN)
                .professionalCategories(List.of())
                .professionals(List.of())
                .weeklySchedules(List.of())
                .build();

        SnapshotCatalogo snapshot = mapper.toDomainModel(response);

        assertThat(snapshot.getCategorias()).isEmpty();
        assertThat(snapshot.getProfesionales()).isEmpty();
        assertThat(snapshot.getHorarios()).isEmpty();
    }

    @Test
    void toDomainModelDevuelveNullSiRecibeNull() {
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
