package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.messaging.mapper;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.AvisoCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.messaging.dto.CatalogUpdatedEvent;

@Component
public class CatalogUpdatedMapper {

    public AvisoCatalogo toDomainModel(CatalogUpdatedEvent evento) {
        if (evento == null) {
            return null;
        }
        return AvisoCatalogo.builder()
                .eventId(evento.getEventId())
                .version(evento.getNewVersion())
                .build();
    }
}
