package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.EstadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResumenSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.ConsultarEstadoSincronizacionUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.IntentoSincronizacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConsultarEstadoSincronizacionUseCaseImpl implements ConsultarEstadoSincronizacionUseCase {

    private final VersionAplicadaRepository versionAplicadaRepository;
    private final IntentoSincronizacionRepository intentoSincronizacionRepository;

    @Override
    public ResumenSincronizacion consultarEstado() {
        VersionAplicada version = versionAplicadaRepository.find().orElse(null);
        IntentoSincronizacion ultimo = intentoSincronizacionRepository.findUltimo().orElse(null);
        return ResumenSincronizacion.builder()
                .estado(calcularEstado(version, ultimo))
                .versionAplicada(version)
                .ultimoIntento(ultimo)
                .build();
    }

    // D-10: el último intento manda sobre la versión; si terminó bien, decide si hay copia.
    private static EstadoSincronizacion calcularEstado(VersionAplicada version, IntentoSincronizacion ultimo) {
        if (ultimo != null && ultimo.estaAbierto()) {
            return EstadoSincronizacion.SINCRONIZANDO;
        }
        if (ultimo != null && ultimo.getResultado() != ResultadoSincronizacion.OK) {
            return EstadoSincronizacion.CON_ERROR;
        }
        return version != null ? EstadoSincronizacion.AL_DIA : EstadoSincronizacion.SIN_DATOS;
    }
}
