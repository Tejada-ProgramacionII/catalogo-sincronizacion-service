package ar.edu.um.programacion2.catalogo.sincronizacion.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import ar.edu.um.programacion2.catalogo.config.SincronizacionProperties;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraRechazoException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoCompletoUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizacionService {

    private final SincronizarCatalogoCompletoUseCase sincronizarCatalogoCompletoUseCase;
    private final SincronizacionProperties sincronizacionProperties;
    private final VersionAplicadaRepository versionAplicadaRepository;

    public void sincronizarSiNoHayCopia() {
        Optional<VersionAplicada> actual = versionAplicadaRepository.find();
        if (actual.isPresent()) {
            log.info("Ya hay una copia local (versión {}): no se sincroniza", actual.get().getVersion());
            return;
        }
        sincronizar();
    }

    public Optional<VersionAplicada> sincronizar() {
        int maxIntentos = sincronizacionProperties.getMaxIntentos();
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                VersionAplicada aplicada = sincronizarCatalogoCompletoUseCase.sincronizar();
                log.info("Sincronización completa aplicada: versión {} (intento {})", aplicada.getVersion(), intento);
                return Optional.of(aplicada);
            } catch (CatedraRechazoException e) {
                log.error("Sincronización completa cancelada sin reintentar: {}", e.getMessage());
                return Optional.empty();
            } catch (CatedraNoDisponibleException e) {
                log.warn("Intento {} de {} de sincronización completa fallido: {}", intento, maxIntentos, e.getMessage());
                if (intento < maxIntentos && !esperarEntreIntentos()) {
                    return Optional.empty();
                }
            }
        }
        log.error("Sincronización completa fallida: se agotaron los {} intentos", maxIntentos);
        return Optional.empty();
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
