package ar.edu.um.programacion2.catalogo.sincronizacion.domain.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvisoCatalogo {
    private String eventId;
    private Long version;
    private Instant procesadoEn;
}
