package ar.edu.um.programacion2.catalogo.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "catedra.sincronizacion")
public class SincronizacionProperties {

    @Min(1)
    private int maxIntentos = 3;

    private Duration esperaEntreIntentos = Duration.ofSeconds(5);

    private boolean alArrancar = true;
}
