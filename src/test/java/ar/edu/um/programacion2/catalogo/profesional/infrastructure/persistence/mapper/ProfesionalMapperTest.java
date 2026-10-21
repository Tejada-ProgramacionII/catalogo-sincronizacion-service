package ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.entity.ProfesionalEntity;

class ProfesionalMapperTest {

    private static final Instant CREADO_EN = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant ACTUALIZADO_EN = Instant.parse("2026-09-10T13:40:00Z");

    private final ProfesionalMapper mapper = new ProfesionalMapper();

    @Test
    void toEntityCopiaTodosLosCampos() {
        Profesional profesional = Profesional.builder()
                .id(15L)
                .categoriaId(1L)
                .nombre("Ana")
                .apellido("Gomez")
                .habilitado(true)
                .creadoEn(CREADO_EN)
                .actualizadoEn(ACTUALIZADO_EN)
                .build();

        ProfesionalEntity entity = mapper.toEntity(profesional);

        assertThat(entity).usingRecursiveComparison().isEqualTo(profesional);
    }

    @Test
    void toDomainModelCopiaTodosLosCampos() {
        ProfesionalEntity entity = ProfesionalEntity.builder()
                .id(16L)
                .categoriaId(2L)
                .nombre("Juan")
                .apellido("Perez")
                .habilitado(false)
                .creadoEn(CREADO_EN)
                .actualizadoEn(ACTUALIZADO_EN)
                .build();

        Profesional profesional = mapper.toDomainModel(entity);

        assertThat(profesional).usingRecursiveComparison().isEqualTo(entity);
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
