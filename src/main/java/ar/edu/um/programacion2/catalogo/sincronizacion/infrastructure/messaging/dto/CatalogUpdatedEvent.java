package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.messaging.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Contrato 15.3. Los campos que no se usan (source, schemaVersion...) se ignoran (15.1).
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogUpdatedEvent {
    private String eventId;
    private Long newVersion;
    private Instant occurredAt;
}
