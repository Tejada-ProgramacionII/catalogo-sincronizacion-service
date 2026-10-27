package ar.edu.um.programacion2.catalogo.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SincronizacionPropertiesTest {

    @EnableConfigurationProperties(SincronizacionProperties.class)
    static class Configuracion {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(Configuracion.class);

    @Test
    void conCeroIntentosElContextoNoLevanta() {
        contextRunner.withPropertyValues("catedra.sincronizacion.max-intentos=0")
                .run(context -> assertThat(context).getFailure()
                        .rootCause()
                        .isInstanceOf(BindValidationException.class));
    }
}
