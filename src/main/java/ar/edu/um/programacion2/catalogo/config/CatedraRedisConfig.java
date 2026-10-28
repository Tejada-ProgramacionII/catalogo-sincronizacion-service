package ar.edu.um.programacion2.catalogo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;

@Configuration
public class CatedraRedisConfig {

    @Bean
    public LettuceConnectionFactory catedraRedisConnectionFactory(CatedraRedisProperties properties) {
        RedisStandaloneConfiguration servidor = new RedisStandaloneConfiguration(properties.getHost(), properties.getPort());
        servidor.setUsername(properties.getUsername());
        servidor.setPassword(properties.getPassword());

        LettuceClientConfiguration cliente = LettuceClientConfiguration.builder()
                .commandTimeout(properties.getTimeout())
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder().connectTimeout(properties.getTimeout()).build())
                        .build())
                .build();
        // La conexión se abre en el primer comando: sin Redis el servicio arranca igual.
        return new LettuceConnectionFactory(servidor, cliente);
    }

    @Bean
    public StringRedisTemplate catedraRedisTemplate(LettuceConnectionFactory catedraRedisConnectionFactory) {
        return new StringRedisTemplate(catedraRedisConnectionFactory);
    }
}
