package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.AvisoCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.AvisoCatalogoEntity;

class AvisoCatalogoMapperTest {

    private static final Instant PROCESADO_EN = Instant.parse("2026-10-28T10:00:00Z");

    private final AvisoCatalogoMapper mapper = new AvisoCatalogoMapper();

    @Test
    void toEntityCopiaLosCampos() {
        AvisoCatalogoEntity entity = mapper.toEntity(
                AvisoCatalogo.builder().eventId("catalog-event-7").version(7L).procesadoEn(PROCESADO_EN).build());

        assertThat(entity.getEventId()).isEqualTo("catalog-event-7");
        assertThat(entity.getVersion()).isEqualTo(7L);
        assertThat(entity.getProcesadoEn()).isEqualTo(PROCESADO_EN);
    }

    @Test
    void toDomainModelCopiaLosCampos() {
        AvisoCatalogo aviso = mapper.toDomainModel(
                AvisoCatalogoEntity.builder().eventId("catalog-event-8").version(8L).procesadoEn(PROCESADO_EN).build());

        assertThat(aviso.getEventId()).isEqualTo("catalog-event-8");
        assertThat(aviso.getVersion()).isEqualTo(8L);
        assertThat(aviso.getProcesadoEn()).isEqualTo(PROCESADO_EN);
    }

    @Test
    void conNullDevuelveNull() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
