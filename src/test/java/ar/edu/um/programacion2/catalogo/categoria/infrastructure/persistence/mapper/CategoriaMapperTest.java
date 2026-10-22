package ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.entity.CategoriaEntity;

class CategoriaMapperTest {

    private static final Instant CREADA_EN = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant ACTUALIZADA_EN = Instant.parse("2026-09-10T13:40:00Z");

    private final CategoriaMapper mapper = new CategoriaMapper();

    @Test
    void toEntityCopiaTodosLosCampos() {
        Categoria categoria = Categoria.builder()
                .id(1L)
                .nombre("Clinica medica")
                .descripcion("Atencion clinica general")
                .habilitada(true)
                .creadaEn(CREADA_EN)
                .actualizadaEn(ACTUALIZADA_EN)
                .build();

        CategoriaEntity entity = mapper.toEntity(categoria);

        assertThat(entity).usingRecursiveComparison().isEqualTo(categoria);
    }

    @Test
    void toDomainModelCopiaTodosLosCampos() {
        CategoriaEntity entity = CategoriaEntity.builder()
                .id(2L)
                .nombre("Pediatria")
                .descripcion("Atencion de ninos")
                .habilitada(false)
                .creadaEn(CREADA_EN)
                .actualizadaEn(ACTUALIZADA_EN)
                .build();

        Categoria categoria = mapper.toDomainModel(entity);

        assertThat(categoria).usingRecursiveComparison().isEqualTo(entity);
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
