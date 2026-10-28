package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.RedisConnectionFailureException;

import ar.edu.um.programacion2.catalogo.config.SchedulingConfig;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.service.SincronizacionService;

class VerificacionPeriodicaSchedulerTest {

    private final SincronizacionService sincronizacionService = mock(SincronizacionService.class);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SchedulingConfig.class, VerificacionPeriodicaScheduler.class)
            .withBean(SincronizacionService.class, () -> sincronizacionService);

    @Test
    void verificaCadaIntervaloConfigurado() {
        contextRunner.withPropertyValues("catedra.sincronizacion.intervalo-verificacion=50ms")
                .run(context -> verify(sincronizacionService, timeout(2000).atLeast(2)).verificar());
    }

    @Test
    void conLaVerificacionPeriodicaDesactivadaNoHayScheduler() {
        contextRunner.withPropertyValues("catedra.sincronizacion.verificacion-periodica=false")
                .run(context -> assertThat(context).doesNotHaveBean(VerificacionPeriodicaScheduler.class));
    }

    @Test
    void unErrorNoCortaLasVerificacionesSiguientes() {
        doThrow(new RedisConnectionFailureException("Redis caído")).when(sincronizacionService).verificar();

        assertThatCode(new VerificacionPeriodicaScheduler(sincronizacionService)::verificar)
                .doesNotThrowAnyException();
        verify(sincronizacionService, atLeast(1)).verificar();
    }
}
