package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.messaging.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.kafka.support.Acknowledgment;

import ar.edu.um.programacion2.catalogo.sincronizacion.application.service.SincronizacionService;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.AvisoCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.messaging.mapper.CatalogUpdatedMapper;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class CatalogUpdatedConsumerTest {

    // Ejemplo de la sección 15.3 del contrato, con un campo que no conocemos.
    private static final String CATALOG_UPDATED = """
            {"eventId": "catalog-event-7-4f3b", "eventType": "CatalogUpdated", "newVersion": 7,
             "occurredAt": "2026-09-10T13:44:59Z", "source": "MANUAL", "schemaVersion": 1, "campoNuevo": true}
            """;

    @Mock
    private SincronizacionService sincronizacionService;

    @Mock
    private Acknowledgment ack;

    private CatalogUpdatedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new CatalogUpdatedConsumer(sincronizacionService, new CatalogUpdatedMapper(),
                JsonMapper.builder().build());
    }

    @Test
    void procesaElAvisoYRecienDespuesConfirmaElOffset() {
        consumer.recibir(CATALOG_UPDATED, ack);

        InOrder orden = inOrder(sincronizacionService, ack);
        ArgumentCaptor<AvisoCatalogo> aviso = ArgumentCaptor.forClass(AvisoCatalogo.class);
        orden.verify(sincronizacionService).procesarAviso(aviso.capture());
        orden.verify(ack).acknowledge();
        assertThat(aviso.getValue().getEventId()).isEqualTo("catalog-event-7-4f3b");
        assertThat(aviso.getValue().getVersion()).isEqualTo(7L);
    }

    @Test
    void unMensajeQueNoEsJsonSeDescartaYSeConfirma() {
        assertThatCode(() -> consumer.recibir("no es json", ack)).doesNotThrowAnyException();

        verifyNoInteractions(sincronizacionService);
        verify(ack).acknowledge();
    }

    @Test
    void unErrorAlProcesarNoDetieneElConsumidor() {
        doThrow(new DataAccessResourceFailureException("base caída")).when(sincronizacionService).procesarAviso(any());

        assertThatCode(() -> consumer.recibir(CATALOG_UPDATED, ack)).doesNotThrowAnyException();

        verify(ack).acknowledge();
    }
}
