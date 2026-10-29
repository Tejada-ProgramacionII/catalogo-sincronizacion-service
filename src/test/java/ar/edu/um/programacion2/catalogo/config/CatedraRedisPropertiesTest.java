package ar.edu.um.programacion2.catalogo.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

class CatedraRedisPropertiesTest {

    @EnableConfigurationProperties(CatedraRedisProperties.class)
    static class Configuracion {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(Configuracion.class, CatedraRedisConfig.class);

    @Test
    void sinClaveElContextoNoLevanta() {
        contextRunner.withPropertyValues("catedra.redis.host=redis.test", "catedra.redis.username=usuario")
                .run(context -> assertThat(context).getFailure()
                        .rootCause()
                        .isInstanceOf(BindValidationException.class));
    }

    @Test
    void conUnPuertoFueraDeRangoElContextoNoLevanta() {
        contextRunner.withPropertyValues("catedra.redis.host=redis.test", "catedra.redis.username=usuario",
                        "catedra.redis.password=clave", "catedra.redis.port=70000")
                .run(context -> assertThat(context).getFailure()
                        .rootCause()
                        .isInstanceOf(BindValidationException.class));
    }

    @Test
    void conLaConfigCompletaArmaLaConexionSinConectarse() {
        contextRunner.withPropertyValues("catedra.redis.host=redis.test", "catedra.redis.port=6380",
                        "catedra.redis.username=usuario", "catedra.redis.password=clave",
                        "catedra.redis.timeout=2s")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    LettuceConnectionFactory factory = context.getBean(LettuceConnectionFactory.class);
                    assertThat(factory.getHostName()).isEqualTo("redis.test");
                    assertThat(factory.getPort()).isEqualTo(6380);
                    assertThat(factory.getStandaloneConfiguration().getUsername()).isEqualTo("usuario");
                    assertThat(factory.getClientConfiguration().getCommandTimeout()).isEqualTo(Duration.ofSeconds(2));
                });
    }
}
