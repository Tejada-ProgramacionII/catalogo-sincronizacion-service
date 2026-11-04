package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.EstadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ProcesoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResumenSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.dto.EstadoSincronizacionResponse;

class SincronizacionDtoMapperTest {

    private static final Instant APLICADA_EN = Instant.parse("2026-10-27T09:00:00Z");
    private static final Instant INICIO = Instant.parse("2026-10-27T10:00:00Z");

    private final SincronizacionDtoMapper mapper = new SincronizacionDtoMapper();

    @Test
    void toEstadoResponseAplanaLaVersionYCopiaElUltimoIntento() {
        ResumenSincronizacion resumen = ResumenSincronizacion.builder()
                .estado(EstadoSincronizacion.CON_ERROR)
                .versionAplicada(VersionAplicada.builder().version(7L).aplicadaEn(APLICADA_EN).build())
                .ultimoIntento(IntentoSincronizacion.builder()
                        .id(3L).proceso(ProcesoSincronizacion.INCREMENTAL).inicio(INICIO).fin(INICIO.plusSeconds(5))
                        .resultado(ResultadoSincronizacion.RECHAZO).intentos(1)
                        .mensaje("La cátedra rechazó el pedido del snapshot").build())
                .ultimaVerificacion(INICIO.plusSeconds(60))
                .build();

        EstadoSincronizacionResponse response = mapper.toEstadoResponse(resumen);

        assertThat(response.getEstado()).isEqualTo(EstadoSincronizacion.CON_ERROR);
        assertThat(response.getVersionAplicada()).isEqualTo(7L);
        assertThat(response.getAplicadaEn()).isEqualTo(APLICADA_EN);
        assertThat(response.getUltimaVerificacion()).isEqualTo(INICIO.plusSeconds(60));
        assertThat(response.getUltimoIntento().getProceso()).isEqualTo(ProcesoSincronizacion.INCREMENTAL);
        assertThat(response.getUltimoIntento().getInicio()).isEqualTo(INICIO);
        assertThat(response.getUltimoIntento().getFin()).isEqualTo(INICIO.plusSeconds(5));
        assertThat(response.getUltimoIntento().getResultado()).isEqualTo(ResultadoSincronizacion.RECHAZO);
        assertThat(response.getUltimoIntento().getIntentos()).isEqualTo(1);
        assertThat(response.getUltimoIntento().getMensaje()).isEqualTo("La cátedra rechazó el pedido del snapshot");
    }

    @Test
    void sinVersionNiIntentosDejaLosCamposEnNull() {
        EstadoSincronizacionResponse response = mapper.toEstadoResponse(
                ResumenSincronizacion.builder().estado(EstadoSincronizacion.SIN_DATOS).build());

        assertThat(response.getEstado()).isEqualTo(EstadoSincronizacion.SIN_DATOS);
        assertThat(response.getVersionAplicada()).isNull();
        assertThat(response.getAplicadaEn()).isNull();
        assertThat(response.getUltimoIntento()).isNull();
    }

    @Test
    void conNullDevuelveNull() {
        assertThat(mapper.toEstadoResponse(null)).isNull();
        assertThat(mapper.toIntentoResponse(null)).isNull();
    }
}
