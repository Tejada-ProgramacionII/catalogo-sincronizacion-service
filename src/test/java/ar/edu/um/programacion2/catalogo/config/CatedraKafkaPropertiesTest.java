package ar.edu.um.programacion2.catalogo.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class CatedraKafkaPropertiesTest {

    @EnableConfigurationProperties(CatedraKafkaProperties.class)
    static class Configuracion {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(Configuracion.class);

    @Test
    void sinTopicElContextoNoLevanta() {
        contextRunner.withPropertyValues("catedra.kafka.bootstrap-servers=kafka.test:9092",
                        "catedra.kafka.grupo=grupo")
                .run(context -> assertThat(context).getFailure()
                        .rootCause()
                        .isInstanceOf(BindValidationException.class));
    }

    @Test
    void conTodoLevanta() {
        contextRunner.withPropertyValues("catedra.kafka.bootstrap-servers=kafka.test:9092",
                        "catedra.kafka.topic-catalogo=catedra.catalog.test", "catedra.kafka.grupo=grupo")
                .run(context -> assertThat(context.getBean(CatedraKafkaProperties.class).getTopicCatalogo())
                        .isEqualTo("catedra.catalog.test"));
    }
}
