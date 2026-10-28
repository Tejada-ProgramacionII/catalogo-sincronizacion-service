package ar.edu.um.programacion2.catalogo.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "catedra.redis")
public class CatedraRedisProperties {

    @NotBlank
    private String host;

    @Min(1)
    @Max(65535)
    private int port = 6379;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private Duration timeout = Duration.ofSeconds(5);
}
