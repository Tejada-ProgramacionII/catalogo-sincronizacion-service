package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.application.service.SincronizacionService;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.messaging.dto.CatalogUpdatedEvent;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.messaging.mapper.CatalogUpdatedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

// D-07: el aviso solo dispara la incremental; los datos salen de Redis.
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogUpdatedConsumer {

    private final SincronizacionService sincronizacionService;
    private final CatalogUpdatedMapper catalogUpdatedMapper;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = "${catedra.kafka.topic-catalogo}", groupId = "${catedra.kafka.grupo}")
    public void recibir(String mensaje, Acknowledgment ack) {
        try {
            CatalogUpdatedEvent evento = jsonMapper.readValue(mensaje, CatalogUpdatedEvent.class);
            log.info("CatalogUpdated {}: versión {}", evento.getEventId(), evento.getNewVersion());
            sincronizacionService.procesarAviso(catalogUpdatedMapper.toDomainModel(evento));
        } catch (RuntimeException e) {
            // Reintentar el mismo mensaje no ayuda: el atraso lo recupera la verificación periódica.
            log.error("Aviso CatalogUpdated no procesado ({}): {}", e.getClass().getSimpleName(), e.getMessage());
        }
        // El offset se confirma recién después de procesar el aviso (contrato 16).
        ack.acknowledge();
    }
}
