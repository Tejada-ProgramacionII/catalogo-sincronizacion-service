package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import java.time.Instant;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.EstadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResumenSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.ConsultarEstadoSincronizacionUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.IntentoSincronizacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.UltimaVerificacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConsultarEstadoSincronizacionUseCaseImpl implements ConsultarEstadoSincronizacionUseCase {

    private final VersionAplicadaRepository versionAplicadaRepository;
    private final IntentoSincronizacionRepository intentoSincronizacionRepository;
    private final UltimaVerificacionRepository ultimaVerificacionRepository;

    @Override
    public ResumenSincronizacion consultarEstado() {
        VersionAplicada version = versionAplicadaRepository.find().orElse(null);
        IntentoSincronizacion ultimo = intentoSincronizacionRepository.findUltimo().orElse(null);
        Instant verificacion = ultimaVerificacionRepository.find().orElse(null);
        return ResumenSincronizacion.builder()
                .estado(calcularEstado(version, ultimo, verificacion))
                .versionAplicada(version)
                .ultimoIntento(ultimo)
                .ultimaVerificacion(verificacion)
                .build();
    }

    // D-13: el último intento manda sobre la versión, salvo un error que una verificación posterior
    // ya dejó atrás (la copia volvió a estar al día sin necesidad de un intento nuevo).
    private static EstadoSincronizacion calcularEstado(VersionAplicada version, IntentoSincronizacion ultimo,
            Instant verificacion) {
        if (ultimo != null && ultimo.estaAbierto()) {
            return EstadoSincronizacion.SINCRONIZANDO;
        }
        if (ultimo != null && ultimo.getResultado() != ResultadoSincronizacion.OK
                && (verificacion == null || !verificacion.isAfter(ultimo.getFin()))) {
            return EstadoSincronizacion.CON_ERROR;
        }
        return version != null ? EstadoSincronizacion.AL_DIA : EstadoSincronizacion.SIN_DATOS;
    }
}
