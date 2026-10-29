package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.CambiosVersion;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.dto.CambiosCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.dto.CambiosVersionCatedraResponse;

class CambiosVersionCacheMapperTest {

    private final CambiosVersionCacheMapper mapper = new CambiosVersionCacheMapper();

    @Test
    void toDomainModelCopiaLaVersionYLosIdsDeCadaColeccion() {
        CambiosVersionCatedraResponse response = CambiosVersionCatedraResponse.builder()
                .version(7L)
                .changes(CambiosCatedraResponse.builder()
                        .professionalCategories(List.of(1L))
                        .professionals(List.of(15L, 16L))
                        .weeklySchedules(List.of())
                        .build())
                .build();

        CambiosVersion cambios = mapper.toDomainModel(response);

        assertThat(cambios.getVersion()).isEqualTo(7L);
        assertThat(cambios.getCategoriaIds()).containsExactly(1L);
        assertThat(cambios.getProfesionalIds()).containsExactly(15L, 16L);
        assertThat(cambios.getHorarioIds()).isEmpty();
    }

    @Test
    void conNullDevuelveNull() {
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
