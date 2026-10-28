package ar.edu.um.programacion2.catalogo.sincronizacion.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import ar.edu.um.programacion2.catalogo.config.SincronizacionProperties;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraRechazoException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.DiscontinuidadException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.AvisoCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ProcesoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResumenSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.ConsultarEstadoSincronizacionUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoCompletoUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoIncrementalUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.VerificarVersionesPendientesUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.AvisoCatalogoRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.IntentoSincronizacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.UltimaVerificacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizacionService {

    // D-11: el registro guarda motivos funcionales fijos; el detalle técnico queda en los logs.
    static final String MENSAJE_RECHAZO = "La cátedra rechazó el pedido de la integración";
    static final String MENSAJE_NO_DISPONIBLE = "La cátedra no respondió después de %d intentos";
    static final String MENSAJE_INTERRUMPIDA = "Se interrumpió la espera entre intentos";
    static final String MENSAJE_ERROR = "Error inesperado al sincronizar el catálogo";
    static final String MENSAJE_REINICIO = "Interrumpida por un reinicio del servicio";

    private final SincronizarCatalogoCompletoUseCase sincronizarCatalogoCompletoUseCase;
    private final SincronizarCatalogoIncrementalUseCase sincronizarCatalogoIncrementalUseCase;
    private final SincronizacionProperties sincronizacionProperties;
    private final VersionAplicadaRepository versionAplicadaRepository;
    private final IntentoSincronizacionRepository intentoSincronizacionRepository;
    private final ConsultarEstadoSincronizacionUseCase consultarEstadoSincronizacionUseCase;
    private final VerificarVersionesPendientesUseCase verificarVersionesPendientesUseCase;
    private final UltimaVerificacionRepository ultimaVerificacionRepository;
    private final AvisoCatalogoRepository avisoCatalogoRepository;

    // D-08: una sola sincronización a la vez, sea cual sea el disparador.
    private final ReentrantLock sincronizando = new ReentrantLock();

    public ResumenSincronizacion consultarEstado() {
        return consultarEstadoSincronizacionUseCase.consultarEstado();
    }

    // D-07: al arrancar y periódicamente. Si otra sincronización está corriendo, esa ya deja la copia verificada.
    public void verificar() {
        if (!sincronizando.tryLock()) {
            log.info("Hay una sincronización en curso: se saltea la verificación");
            return;
        }
        try {
            if (hayVersionesPendientes()) {
                sincronizarIncremental();
            } else {
                // Sin nada que aplicar no se registra un intento: solo cuándo se verificó.
                ultimaVerificacionRepository.save(Instant.now());
            }
        } finally {
            sincronizando.unlock();
        }
    }

    // CU-02, pasos 1 y 2. Espera a la sincronización en curso: el aviso se confirma recién después de aplicar.
    public void procesarAviso(AvisoCatalogo aviso) {
        sincronizando.lock();
        try {
            if (avisoCatalogoRepository.existsByEventId(aviso.getEventId())) {
                log.info("Aviso {} repetido: ya se procesó", aviso.getEventId());
                return;
            }
            Optional<VersionAplicada> local = versionAplicadaRepository.find();
            boolean yaIncorporado = local.isPresent() && aviso.getVersion() <= local.get().getVersion();
            // Si la incremental falla no se marca: un reenvío del aviso vuelve a intentar (D-05 lo hace inofensivo).
            if (yaIncorporado || sincronizarIncremental().isPresent()) {
                aviso.setProcesadoEn(Instant.now());
                avisoCatalogoRepository.save(aviso);
            }
        } finally {
            sincronizando.unlock();
        }
    }

    private boolean hayVersionesPendientes() {
        try {
            return verificarVersionesPendientesUseCase.hayVersionesPendientes();
        } catch (CatedraNoDisponibleException | CatedraRechazoException e) {
            // La incremental reintenta y, si sigue fallando, deja el error registrado (CON_ERROR).
            log.warn("No se pudo verificar la versión publicada: {}", e.getMessage());
            return true;
        }
    }

    public void cerrarIntentosInterrumpidos() {
        List<IntentoSincronizacion> abiertos = intentoSincronizacionRepository.findAbiertos();
        abiertos.forEach(intento -> cerrar(intento, ResultadoSincronizacion.ERROR, MENSAJE_REINICIO));
        if (!abiertos.isEmpty()) {
            log.warn("Sincronizaciones interrumpidas por un reinicio cerradas como error: {}", abiertos.size());
        }
    }

    // CU-01
    public Optional<VersionAplicada> sincronizar() {
        sincronizando.lock();
        try {
            return registrar(ProcesoSincronizacion.COMPLETA,
                    registro -> conReintentos(registro, sincronizarCatalogoCompletoUseCase::sincronizar));
        } finally {
            sincronizando.unlock();
        }
    }

    // CU-02, y CU-03 si no puede continuar desde la versión local.
    public Optional<VersionAplicada> sincronizarIncremental() {
        sincronizando.lock();
        try {
            if (versionAplicadaRepository.find().isEmpty()) {
                log.info("Sin copia local: en lugar de la incremental se hace la sincronización completa");
                return sincronizar();
            }
            return registrar(ProcesoSincronizacion.INCREMENTAL, registro -> {
                try {
                    return conReintentos(registro, sincronizarCatalogoIncrementalUseCase::sincronizar);
                } catch (DiscontinuidadException e) {
                    return recuperar(registro, e);
                }
            });
        } finally {
            sincronizando.unlock();
        }
    }

    // CU-03. Una segunda discontinuidad no se atrapa acá: llega a registrar() y termina (D-08).
    private VersionAplicada recuperar(IntentoSincronizacion registro, DiscontinuidadException motivo) {
        log.warn("Discontinuidad de versiones ({}): se recupera con un snapshot", motivo.getMessage());
        registro.setProceso(ProcesoSincronizacion.RECUPERACION);
        registro.setMensaje(motivo.getMessage());
        intentoSincronizacionRepository.save(registro);

        VersionAplicada snapshot = conReintentos(registro, sincronizarCatalogoCompletoUseCase::sincronizar);
        registro.setVersionAplicada(snapshot.getVersion());
        return conReintentos(registro, sincronizarCatalogoIncrementalUseCase::sincronizar);
    }

    // Abre el registro (CU-06), ejecuta la sincronización y lo cierra según cómo terminó.
    private Optional<VersionAplicada> registrar(ProcesoSincronizacion proceso,
            Function<IntentoSincronizacion, VersionAplicada> sincronizacion) {
        IntentoSincronizacion registro = intentoSincronizacionRepository.save(
                IntentoSincronizacion.builder().proceso(proceso).inicio(Instant.now()).build());
        try {
            VersionAplicada aplicada = sincronizacion.apply(registro);
            log.info("Sincronización {} terminada: versión {} ({} intentos)",
                    registro.getProceso(), aplicada.getVersion(), registro.getIntentos());
            registro.setVersionAplicada(aplicada.getVersion());
            cerrar(registro, ResultadoSincronizacion.OK, registro.getMensaje());
            return Optional.of(aplicada);
        } catch (CatedraRechazoException e) {
            log.error("Sincronización {} cancelada sin reintentar: {}", registro.getProceso(), e.getMessage());
            cerrar(registro, ResultadoSincronizacion.RECHAZO, MENSAJE_RECHAZO);
        } catch (CatedraNoDisponibleException e) {
            log.error("Sincronización {} fallida: {}", registro.getProceso(), registro.getMensaje());
            cerrar(registro, ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE, registro.getMensaje());
        } catch (DiscontinuidadException e) {
            log.error("Sincronización {} sin poder continuar: {}", registro.getProceso(), e.getMessage());
            cerrar(registro, ResultadoSincronizacion.DISCONTINUIDAD, e.getMessage());
        } catch (RuntimeException e) {
            cerrar(registro, ResultadoSincronizacion.ERROR, MENSAJE_ERROR);
            throw e;
        }
        return Optional.empty();
    }

    // D-03: reintenta solo si la cátedra no está disponible. Al rendirse deja el motivo en el registro.
    private VersionAplicada conReintentos(IntentoSincronizacion registro, Supplier<VersionAplicada> paso) {
        int maxIntentos = sincronizacionProperties.getMaxIntentos();
        for (int intento = 1; ; intento++) {
            registro.setIntentos(registro.getIntentos() + 1);
            try {
                return paso.get();
            } catch (CatedraNoDisponibleException e) {
                log.warn("Intento {} de {} de sincronización {} fallido: {}",
                        intento, maxIntentos, registro.getProceso(), e.getMessage());
                intentoSincronizacionRepository.save(registro);
                if (intento >= maxIntentos) {
                    registro.setMensaje(String.format(MENSAJE_NO_DISPONIBLE, maxIntentos));
                    throw e;
                }
                if (!esperarEntreIntentos()) {
                    registro.setMensaje(MENSAJE_INTERRUMPIDA);
                    throw e;
                }
            }
        }
    }

    private void cerrar(IntentoSincronizacion registro, ResultadoSincronizacion resultado, String mensaje) {
        registro.setFin(Instant.now());
        registro.setResultado(resultado);
        registro.setMensaje(mensaje);
        intentoSincronizacionRepository.save(registro);
    }

    private boolean esperarEntreIntentos() {
        try {
            Thread.sleep(sincronizacionProperties.getEsperaEntreIntentos());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sincronización cancelada: se interrumpió la espera entre intentos");
            return false;
        }
    }
}
