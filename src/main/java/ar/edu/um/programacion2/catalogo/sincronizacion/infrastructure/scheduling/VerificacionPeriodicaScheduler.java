package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.scheduling;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.application.service.SincronizacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// D-07: recupera los avisos de Kafka que se hayan perdido comparando la versión local con la publicada.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "catedra.sincronizacion.verificacion-periodica", havingValue = "true",
        matchIfMissing = true)
public class VerificacionPeriodicaScheduler {

    private final SincronizacionService sincronizacionService;

    // La primera vuelta espera un intervalo: al arrancar ya verifica el listener.
    @Scheduled(fixedDelayString = "${catedra.sincronizacion.intervalo-verificacion:5m}",
            initialDelayString = "${catedra.sincronizacion.intervalo-verificacion:5m}")
    public void verificar() {
        try {
            sincronizacionService.verificar();
        } catch (RuntimeException e) {
            log.error("Verificación periódica fallida ({}): {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
