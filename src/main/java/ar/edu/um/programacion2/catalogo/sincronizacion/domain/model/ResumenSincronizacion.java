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
public class ResumenSincronizacion {
    private EstadoSincronizacion estado;
    private VersionAplicada versionAplicada;
    private IntentoSincronizacion ultimoIntento;
    private Instant ultimaVerificacion;
}
