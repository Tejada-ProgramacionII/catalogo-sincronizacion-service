package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository.JpaUltimaVerificacionRepository;

@SpringBootTest
@Transactional
class JpaUltimaVerificacionRepositoryAdapterTest {

    @Autowired
    private JpaUltimaVerificacionRepositoryAdapter adapter;

    @Autowired
    private JpaUltimaVerificacionRepository jpaUltimaVerificacionRepository;

    @Test
    void sinVerificacionesDevuelveVacio() {
        assertThat(adapter.find()).isEmpty();
    }

    @Test
    void cadaVerificacionPisaLaAnteriorEnUnaSolaFila() {
        adapter.save(Instant.parse("2026-10-28T10:00:00Z"));
        adapter.save(Instant.parse("2026-10-28T10:05:00Z"));

        assertThat(adapter.find()).contains(Instant.parse("2026-10-28T10:05:00Z"));
        assertThat(jpaUltimaVerificacionRepository.count()).isEqualTo(1);
    }
}
