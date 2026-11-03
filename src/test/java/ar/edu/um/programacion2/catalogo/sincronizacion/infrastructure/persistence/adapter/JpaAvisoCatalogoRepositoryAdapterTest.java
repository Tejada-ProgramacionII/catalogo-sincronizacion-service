package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.AvisoCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository.JpaAvisoCatalogoRepository;

@SpringBootTest
@Transactional
class JpaAvisoCatalogoRepositoryAdapterTest {

    @Autowired
    private JpaAvisoCatalogoRepositoryAdapter adapter;

    @Autowired
    private JpaAvisoCatalogoRepository jpaAvisoCatalogoRepository;

    @Test
    void unAvisoGuardadoSeReconocePorSuEventId() {
        adapter.save(AvisoCatalogo.builder().eventId("catalog-event-7-4f3b").version(7L)
                .procesadoEn(Instant.parse("2026-10-28T10:00:00Z")).build());

        assertThat(adapter.existsByEventId("catalog-event-7-4f3b")).isTrue();
        assertThat(adapter.existsByEventId("catalog-event-8-0000")).isFalse();
        assertThat(jpaAvisoCatalogoRepository.findById("catalog-event-7-4f3b")).hasValueSatisfying(
                aviso -> assertThat(aviso.getVersion()).isEqualTo(7L));
    }
}
