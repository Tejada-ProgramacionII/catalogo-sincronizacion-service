package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.dto;

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
public class CambiosVersionCatedraResponse {
    private Long version;
    private CambiosCatedraResponse changes;
}
