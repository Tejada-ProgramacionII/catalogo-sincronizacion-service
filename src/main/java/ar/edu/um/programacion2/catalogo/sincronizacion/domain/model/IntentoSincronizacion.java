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
public class IntentoSincronizacion {
    private Long id;
    private Instant inicio;
    private Instant fin;
    private ResultadoSincronizacion resultado;
    private int intentos;
    private Long versionAplicada;
    private String mensaje;

    public boolean estaAbierto() {
        return fin == null;
    }
}
