package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.startup;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.config.SincronizacionProperties;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.service.SincronizacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SincronizacionAlArrancarListener {

    private final SincronizacionService sincronizacionService;
    private final SincronizacionProperties sincronizacionProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void sincronizarAlArrancar() {
        try {
            // Una sincronización que quedó abierta murió con el proceso anterior.
            sincronizacionService.cerrarIntentosInterrumpidos();
            if (sincronizacionProperties.isAlArrancar()) {
                sincronizacionService.sincronizarSiNoHayCopia();
            }
        } catch (RuntimeException e) {
            log.error("Sincronización al arrancar fallida ({}): {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
