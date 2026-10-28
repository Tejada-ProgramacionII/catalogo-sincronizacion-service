package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.dto;

import java.time.Instant;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
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
public class IntentoSincronizacionResponse {
    private Instant inicio;
    private Instant fin;
    private ResultadoSincronizacion resultado;
    private int intentos;
    private String mensaje;
}
