package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.dto;

import java.time.Instant;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.EstadoSincronizacion;
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
public class EstadoSincronizacionResponse {
    private EstadoSincronizacion estado;
    private Long versionAplicada;
    private Instant aplicadaEn;
    private IntentoSincronizacionResponse ultimoIntento;
}
