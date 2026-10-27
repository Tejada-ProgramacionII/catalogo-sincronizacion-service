package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto;

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
public class CategoriaCatedraResponse {
    private Long id;
    private String name;
    private String description;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
