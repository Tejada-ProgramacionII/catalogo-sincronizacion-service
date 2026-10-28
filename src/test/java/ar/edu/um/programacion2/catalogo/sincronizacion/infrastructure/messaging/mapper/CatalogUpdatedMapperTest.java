package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.AvisoCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.messaging.dto.CatalogUpdatedEvent;

class CatalogUpdatedMapperTest {

    private final CatalogUpdatedMapper mapper = new CatalogUpdatedMapper();

    @Test
    void toDomainModelCopiaElEventIdYLaVersion() {
        AvisoCatalogo aviso = mapper.toDomainModel(
                CatalogUpdatedEvent.builder().eventId("catalog-event-7").newVersion(7L).build());

        assertThat(aviso.getEventId()).isEqualTo("catalog-event-7");
        assertThat(aviso.getVersion()).isEqualTo(7L);
        assertThat(aviso.getProcesadoEn()).isNull();
    }

    @Test
    void conNullDevuelveNull() {
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
