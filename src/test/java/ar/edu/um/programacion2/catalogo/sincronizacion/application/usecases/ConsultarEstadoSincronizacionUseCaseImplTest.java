package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.EstadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResumenSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.IntentoSincronizacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;

@ExtendWith(MockitoExtension.class)
class ConsultarEstadoSincronizacionUseCaseImplTest {

    private static final Instant INICIO = Instant.parse("2026-10-27T10:00:00Z");
    private static final VersionAplicada VERSION = VersionAplicada.builder().version(7L).aplicadaEn(INICIO).build();

    @Mock
    private VersionAplicadaRepository versionAplicadaRepository;

    @Mock
    private IntentoSincronizacionRepository intentoSincronizacionRepository;

    @InjectMocks
    private ConsultarEstadoSincronizacionUseCaseImpl useCase;

    @Test
    void sinVersionNiIntentosEstaSinDatos() {
        dado(null, null);

        ResumenSincronizacion resumen = useCase.consultarEstado();

        assertThat(resumen.getEstado()).isEqualTo(EstadoSincronizacion.SIN_DATOS);
        assertThat(resumen.getVersionAplicada()).isNull();
        assertThat(resumen.getUltimoIntento()).isNull();
    }

    @Test
    void conUnIntentoAbiertoEstaSincronizandoAunqueNoHayaVersion() {
        IntentoSincronizacion abierto = IntentoSincronizacion.builder().inicio(INICIO).intentos(1).build();
        dado(null, abierto);

        ResumenSincronizacion resumen = useCase.consultarEstado();

        assertThat(resumen.getEstado()).isEqualTo(EstadoSincronizacion.SINCRONIZANDO);
        assertThat(resumen.getUltimoIntento()).isSameAs(abierto);
    }

    @Test
    void conUnIntentoAbiertoEstaSincronizandoAunqueHayaVersion() {
        dado(VERSION, IntentoSincronizacion.builder().inicio(INICIO).build());

        assertThat(useCase.consultarEstado().getEstado()).isEqualTo(EstadoSincronizacion.SINCRONIZANDO);
    }

    @Test
    void siElUltimoIntentoFallaEstaConErrorYSigueInformandoLaCopiaAnterior() {
        dado(VERSION, cerrado(ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE));

        ResumenSincronizacion resumen = useCase.consultarEstado();

        assertThat(resumen.getEstado()).isEqualTo(EstadoSincronizacion.CON_ERROR);
        assertThat(resumen.getVersionAplicada()).isSameAs(VERSION);
        assertThat(resumen.getUltimoIntento().getResultado()).isEqualTo(ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE);
    }

    @Test
    void siLaPrimeraSincronizacionFallaEstaConErrorSinVersion() {
        dado(null, cerrado(ResultadoSincronizacion.RECHAZO));

        ResumenSincronizacion resumen = useCase.consultarEstado();

        assertThat(resumen.getEstado()).isEqualTo(EstadoSincronizacion.CON_ERROR);
        assertThat(resumen.getVersionAplicada()).isNull();
    }

    @Test
    void conVersionYElUltimoIntentoOkEstaAlDia() {
        dado(VERSION, cerrado(ResultadoSincronizacion.OK));

        ResumenSincronizacion resumen = useCase.consultarEstado();

        assertThat(resumen.getEstado()).isEqualTo(EstadoSincronizacion.AL_DIA);
        assertThat(resumen.getVersionAplicada()).isSameAs(VERSION);
    }

    @Test
    void conVersionYSinIntentosRegistradosEstaAlDia() {
        dado(VERSION, null);

        assertThat(useCase.consultarEstado().getEstado()).isEqualTo(EstadoSincronizacion.AL_DIA);
    }

    private void dado(VersionAplicada version, IntentoSincronizacion ultimo) {
        when(versionAplicadaRepository.find()).thenReturn(Optional.ofNullable(version));
        when(intentoSincronizacionRepository.findUltimo()).thenReturn(Optional.ofNullable(ultimo));
    }

    private static IntentoSincronizacion cerrado(ResultadoSincronizacion resultado) {
        return IntentoSincronizacion.builder()
                .inicio(INICIO).fin(INICIO.plusSeconds(5)).resultado(resultado).intentos(1).build();
    }
}
