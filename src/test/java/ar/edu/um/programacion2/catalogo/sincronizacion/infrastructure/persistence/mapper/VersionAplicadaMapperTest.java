package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.VersionAplicadaEntity;

class VersionAplicadaMapperTest {

    private static final Instant APLICADA_EN = Instant.parse("2026-09-10T13:44:59Z");

    private final VersionAplicadaMapper mapper = new VersionAplicadaMapper();

    @Test
    void toEntityUsaSiempreElIdUnico() {
        VersionAplicada versionAplicada = VersionAplicada.builder()
                .version(7L)
                .aplicadaEn(APLICADA_EN)
                .build();

        VersionAplicadaEntity entity = mapper.toEntity(versionAplicada);

        assertThat(entity.getId()).isEqualTo(VersionAplicadaEntity.ID_UNICO);
        assertThat(entity.getVersion()).isEqualTo(7L);
        assertThat(entity.getAplicadaEn()).isEqualTo(APLICADA_EN);
    }

    @Test
    void toDomainModelCopiaLaVersionYLaFecha() {
        VersionAplicadaEntity entity = VersionAplicadaEntity.builder()
                .id(VersionAplicadaEntity.ID_UNICO)
                .version(8L)
                .aplicadaEn(APLICADA_EN)
                .build();

        VersionAplicada versionAplicada = mapper.toDomainModel(entity);

        assertThat(versionAplicada.getVersion()).isEqualTo(8L);
        assertThat(versionAplicada.getAplicadaEn()).isEqualTo(APLICADA_EN);
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
