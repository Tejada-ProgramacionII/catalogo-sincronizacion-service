package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ProcesoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;

@SpringBootTest
@Transactional
class JpaIntentoSincronizacionRepositoryAdapterTest {

    private static final Instant INICIO = Instant.parse("2026-10-27T10:00:00Z");

    @Autowired
    private JpaIntentoSincronizacionRepositoryAdapter adapter;

    @Test
    void saveAsignaElIdYActualizaElMismoIntento() {
        IntentoSincronizacion abierto = adapter.save(IntentoSincronizacion.builder()
                .proceso(ProcesoSincronizacion.INCREMENTAL).inicio(INICIO).build());

        abierto.setFin(INICIO.plusSeconds(5));
        abierto.setResultado(ResultadoSincronizacion.OK);
        abierto.setIntentos(1);
        abierto.setVersionAplicada(7L);
        IntentoSincronizacion cerrado = adapter.save(abierto);

        assertThat(cerrado.getId()).isNotNull().isEqualTo(abierto.getId());
        assertThat(adapter.findUltimo()).hasValueSatisfying(ultimo -> {
            assertThat(ultimo.getResultado()).isEqualTo(ResultadoSincronizacion.OK);
            assertThat(ultimo.getVersionAplicada()).isEqualTo(7L);
            assertThat(ultimo.getProceso()).isEqualTo(ProcesoSincronizacion.INCREMENTAL);
        });
    }

    @Test
    void findUltimoDevuelveElUltimoRegistrado() {
        adapter.save(cerrado(ResultadoSincronizacion.OK));
        IntentoSincronizacion ultimo = adapter.save(cerrado(ResultadoSincronizacion.RECHAZO));

        assertThat(adapter.findUltimo()).hasValueSatisfying(
                encontrado -> assertThat(encontrado.getId()).isEqualTo(ultimo.getId()));
    }

    @Test
    void findUltimoSinIntentosDevuelveVacio() {
        assertThat(adapter.findUltimo()).isEmpty();
    }

    @Test
    void findAbiertosDevuelveSoloLosQueNoTerminaron() {
        adapter.save(cerrado(ResultadoSincronizacion.OK));
        IntentoSincronizacion abierto = adapter.save(IntentoSincronizacion.builder()
                .proceso(ProcesoSincronizacion.INCREMENTAL).inicio(INICIO).build());

        assertThat(adapter.findAbiertos()).extracting(IntentoSincronizacion::getId).containsExactly(abierto.getId());
    }

    private static IntentoSincronizacion cerrado(ResultadoSincronizacion resultado) {
        return IntentoSincronizacion.builder()
                .proceso(ProcesoSincronizacion.COMPLETA).inicio(INICIO).fin(INICIO.plusSeconds(5))
                .resultado(resultado).intentos(1).build();
    }
}
