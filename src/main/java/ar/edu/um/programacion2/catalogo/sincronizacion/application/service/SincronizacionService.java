package ar.edu.um.programacion2.catalogo.sincronizacion.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ar.edu.um.programacion2.catalogo.config.SincronizacionProperties;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraRechazoException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ProcesoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResumenSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.ConsultarEstadoSincronizacionUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoCompletoUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.IntentoSincronizacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizacionService {

    // D-11: el registro guarda motivos funcionales fijos; el detalle técnico queda en los logs.
    static final String MENSAJE_RECHAZO = "La cátedra rechazó el pedido del snapshot";
    static final String MENSAJE_NO_DISPONIBLE = "La cátedra no respondió después de %d intentos";
    static final String MENSAJE_INTERRUMPIDA = "Se interrumpió la espera entre intentos";
    static final String MENSAJE_ERROR = "Error inesperado al aplicar el snapshot";
    static final String MENSAJE_REINICIO = "Interrumpida por un reinicio del servicio";

    private final SincronizarCatalogoCompletoUseCase sincronizarCatalogoCompletoUseCase;
    private final SincronizacionProperties sincronizacionProperties;
    private final VersionAplicadaRepository versionAplicadaRepository;
    private final IntentoSincronizacionRepository intentoSincronizacionRepository;
    private final ConsultarEstadoSincronizacionUseCase consultarEstadoSincronizacionUseCase;

    public ResumenSincronizacion consultarEstado() {
        return consultarEstadoSincronizacionUseCase.consultarEstado();
    }

    public void sincronizarSiNoHayCopia() {
        Optional<VersionAplicada> actual = versionAplicadaRepository.find();
        if (actual.isPresent()) {
            log.info("Ya hay una copia local (versión {}): no se sincroniza", actual.get().getVersion());
            return;
        }
        sincronizar();
    }

    public void cerrarIntentosInterrumpidos() {
        List<IntentoSincronizacion> abiertos = intentoSincronizacionRepository.findAbiertos();
        abiertos.forEach(intento -> cerrar(intento, ResultadoSincronizacion.ERROR, null, MENSAJE_REINICIO));
        if (!abiertos.isEmpty()) {
            log.warn("Sincronizaciones interrumpidas por un reinicio cerradas como error: {}", abiertos.size());
        }
    }

    public Optional<VersionAplicada> sincronizar() {
        IntentoSincronizacion registro = intentoSincronizacionRepository.save(
                IntentoSincronizacion.builder().proceso(ProcesoSincronizacion.COMPLETA).inicio(Instant.now()).build());
        try {
            return sincronizarConReintentos(registro);
        } catch (RuntimeException e) {
            cerrar(registro, ResultadoSincronizacion.ERROR, null, MENSAJE_ERROR);
            throw e;
        }
    }

    private Optional<VersionAplicada> sincronizarConReintentos(IntentoSincronizacion registro) {
        int maxIntentos = sincronizacionProperties.getMaxIntentos();
        for (int intento = 1; intento <= maxIntentos; intento++) {
            registro.setIntentos(intento);
            try {
                VersionAplicada aplicada = sincronizarCatalogoCompletoUseCase.sincronizar();
                log.info("Sincronización completa aplicada: versión {} (intento {})", aplicada.getVersion(), intento);
                cerrar(registro, ResultadoSincronizacion.OK, aplicada.getVersion(), null);
                return Optional.of(aplicada);
            } catch (CatedraRechazoException e) {
                log.error("Sincronización completa cancelada sin reintentar: {}", e.getMessage());
                cerrar(registro, ResultadoSincronizacion.RECHAZO, null, MENSAJE_RECHAZO);
                return Optional.empty();
            } catch (CatedraNoDisponibleException e) {
                log.warn("Intento {} de {} de sincronización completa fallido: {}", intento, maxIntentos, e.getMessage());
                intentoSincronizacionRepository.save(registro);
                if (intento < maxIntentos && !esperarEntreIntentos()) {
                    cerrar(registro, ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE, null, MENSAJE_INTERRUMPIDA);
                    return Optional.empty();
                }
            }
        }
        log.error("Sincronización completa fallida: se agotaron los {} intentos", maxIntentos);
        cerrar(registro, ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE, null,
                String.format(MENSAJE_NO_DISPONIBLE, maxIntentos));
        return Optional.empty();
    }

    private void cerrar(IntentoSincronizacion registro, ResultadoSincronizacion resultado, Long version, String mensaje) {
        registro.setFin(Instant.now());
        registro.setResultado(resultado);
        registro.setVersionAplicada(version);
        registro.setMensaje(mensaje);
        intentoSincronizacionRepository.save(registro);
    }

    private boolean esperarEntreIntentos() {
        try {
            Thread.sleep(sincronizacionProperties.getEsperaEntreIntentos());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sincronización completa cancelada: se interrumpió la espera entre intentos");
            return false;
        }
    }
}
