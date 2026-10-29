package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.adapter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import ar.edu.um.programacion2.catalogo.config.CatedraRedisConfig;
import ar.edu.um.programacion2.catalogo.config.CatedraRedisProperties;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.mapper.CambiosVersionCacheMapper;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.mapper.SnapshotCatalogoClientMapper;
import tools.jackson.databind.json.JsonMapper;

// Con la conexión real de CatedraRedisConfig apuntando a un puerto donde no escucha nadie.
class RedisCatalogoPublicadoClientAdapterSinRedisTest {

    private LettuceConnectionFactory factory;
    private RedisCatalogoPublicadoClientAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        int puertoLibre;
        try (ServerSocket socket = new ServerSocket(0)) {
            puertoLibre = socket.getLocalPort();
        }
        CatedraRedisProperties properties = new CatedraRedisProperties();
        properties.setHost("localhost");
        properties.setPort(puertoLibre);
        properties.setUsername("usuario");
        properties.setPassword("clave");
        properties.setTimeout(Duration.ofMillis(500));

        CatedraRedisConfig config = new CatedraRedisConfig();
        factory = config.catedraRedisConnectionFactory(properties);
        factory.afterPropertiesSet();
        adapter = new RedisCatalogoPublicadoClientAdapter(config.catedraRedisTemplate(factory),
                JsonMapper.builder().build(), new CambiosVersionCacheMapper(), new SnapshotCatalogoClientMapper());
    }

    @AfterEach
    void tearDown() {
        factory.destroy();
    }

    @Test
    void getVersionesConRedisCaidoLanzaNoDisponible() {
        assertThatThrownBy(adapter::getVersiones)
                .isInstanceOf(CatedraNoDisponibleException.class)
                .hasMessage("No se pudo leer el catálogo publicado en Redis")
                .hasMessageNotContaining("clave");
    }
}
