package ar.edu.um.programacion2.catalogo.sincronizacion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

import ar.edu.um.programacion2.catalogo.config.SincronizacionProperties;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraRechazoException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.DiscontinuidadException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ProcesoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.EstadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResumenSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.ConsultarEstadoSincronizacionUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoCompletoUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoIncrementalUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.VerificarVersionesPendientesUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.IntentoSincronizacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.UltimaVerificacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SincronizacionServiceTest {

    private static final DiscontinuidadException DISCONTINUIDAD =
            new DiscontinuidadException("La versión local 5 es anterior a la más antigua disponible (6)");

    private static final CatedraNoDisponibleException NO_DISPONIBLE =
            new CatedraNoDisponibleException("La cátedra respondió con un error (HTTP 503)", null);

    @Mock
    private SincronizarCatalogoCompletoUseCase sincronizarCatalogoCompletoUseCase;

    @Mock
    private SincronizarCatalogoIncrementalUseCase sincronizarCatalogoIncrementalUseCase;

    @Mock
    private VersionAplicadaRepository versionAplicadaRepository;

    @Mock
    private IntentoSincronizacionRepository intentoSincronizacionRepository;

    @Mock
    private ConsultarEstadoSincronizacionUseCase consultarEstadoSincronizacionUseCase;

    @Mock
    private VerificarVersionesPendientesUseCase verificarVersionesPendientesUseCase;

    @Mock
    private UltimaVerificacionRepository ultimaVerificacionRepository;

    private SincronizacionProperties sincronizacionProperties;

    private SincronizacionService service;

    // Foto de cada save: el service reusa el mismo objeto, así que se guarda una copia.
    private final List<IntentoSincronizacion> guardados = new ArrayList<>();

    @BeforeEach
    void setUp() {
        sincronizacionProperties = new SincronizacionProperties();
        sincronizacionProperties.setMaxIntentos(3);
        sincronizacionProperties.setEsperaEntreIntentos(Duration.ZERO);
        service = new SincronizacionService(sincronizarCatalogoCompletoUseCase, sincronizarCatalogoIncrementalUseCase,
                sincronizacionProperties,
                versionAplicadaRepository, intentoSincronizacionRepository, consultarEstadoSincronizacionUseCase,
                verificarVersionesPendientesUseCase, ultimaVerificacionRepository);
        when(intentoSincronizacionRepository.save(any())).thenAnswer(invocacion -> {
            IntentoSincronizacion intento = invocacion.getArgument(0);
            guardados.add(copia(intento));
            if (intento.getId() == null) {
                intento.setId(1L);
            }
            return intento;
        });
    }

    @Test
    void reintentaHastaQueLaCatedraRespondeYRegistraCadaPaso() {
        VersionAplicada version = VersionAplicada.builder().version(7L).aplicadaEn(Instant.now()).build();
        when(sincronizarCatalogoCompletoUseCase.sincronizar())
                .thenThrow(NO_DISPONIBLE)
                .thenThrow(NO_DISPONIBLE)
                .thenReturn(version);

        assertThat(service.sincronizar()).contains(version);
        verify(sincronizarCatalogoCompletoUseCase, times(3)).sincronizar();

        // Al empezar, al fin de cada intento fallido y al terminar.
        assertThat(guardados).extracting(IntentoSincronizacion::getIntentos).containsExactly(0, 1, 2, 3);
        assertThat(guardados.subList(0, 3)).allSatisfy(paso -> {
            assertThat(paso.estaAbierto()).isTrue();
            assertThat(paso.getResultado()).isNull();
        });
        IntentoSincronizacion fin = ultimoGuardado();
        assertThat(fin.getId()).isEqualTo(1L);
        assertThat(fin.getProceso()).isEqualTo(ProcesoSincronizacion.COMPLETA);
        assertThat(fin.getFin()).isNotNull();
        assertThat(fin.getResultado()).isEqualTo(ResultadoSincronizacion.OK);
        assertThat(fin.getVersionAplicada()).isEqualTo(7L);
        assertThat(fin.getMensaje()).isNull();
    }

    @Test
    void siLaCatedraFallaSiempreHaceMaxIntentosYRegistraQueNoRespondio() {
        when(sincronizarCatalogoCompletoUseCase.sincronizar()).thenThrow(NO_DISPONIBLE);

        assertThat(service.sincronizar()).isEmpty();
        verify(sincronizarCatalogoCompletoUseCase, times(3)).sincronizar();

        IntentoSincronizacion fin = ultimoGuardado();
        assertThat(fin.getResultado()).isEqualTo(ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE);
        assertThat(fin.getIntentos()).isEqualTo(3);
        assertThat(fin.getVersionAplicada()).isNull();
        assertThat(fin.getMensaje()).isEqualTo("La cátedra no respondió después de 3 intentos")
                .doesNotContain("HTTP");
    }

    @Test
    void siLaCatedraRechazaElPedidoNoReintentaYRegistraElRechazo() {
        when(sincronizarCatalogoCompletoUseCase.sincronizar())
                .thenThrow(new CatedraRechazoException("La cátedra rechazó el pedido (HTTP 401)", null));

        assertThat(service.sincronizar()).isEmpty();
        verify(sincronizarCatalogoCompletoUseCase).sincronizar();

        IntentoSincronizacion fin = ultimoGuardado();
        assertThat(fin.getResultado()).isEqualTo(ResultadoSincronizacion.RECHAZO);
        assertThat(fin.getIntentos()).isEqualTo(1);
        assertThat(fin.getMensaje()).isEqualTo(SincronizacionService.MENSAJE_RECHAZO);
    }

    @Test
    void unErrorInesperadoSeRegistraSinElDetalleYSePropaga() {
        DataIntegrityViolationException error = new DataIntegrityViolationException("violación de fk_horarios");
        when(sincronizarCatalogoCompletoUseCase.sincronizar()).thenThrow(error);

        assertThatThrownBy(service::sincronizar).isSameAs(error);

        IntentoSincronizacion fin = ultimoGuardado();
        assertThat(fin.getResultado()).isEqualTo(ResultadoSincronizacion.ERROR);
        assertThat(fin.getFin()).isNotNull();
        assertThat(fin.getMensaje()).isEqualTo(SincronizacionService.MENSAJE_ERROR).doesNotContain("fk_horarios");
    }

    @Test
    void siSeInterrumpeLaEsperaCortaYConservaLaMarca() {
        sincronizacionProperties.setEsperaEntreIntentos(Duration.ofSeconds(1));
        when(sincronizarCatalogoCompletoUseCase.sincronizar()).thenThrow(NO_DISPONIBLE);

        Optional<VersionAplicada> resultado;
        boolean marcaPuesta;
        Thread.currentThread().interrupt();
        try {
            resultado = service.sincronizar();
        } finally {
            marcaPuesta = Thread.interrupted();
        }

        assertThat(resultado).isEmpty();
        assertThat(marcaPuesta).isTrue();
        verify(sincronizarCatalogoCompletoUseCase).sincronizar();
        assertThat(ultimoGuardado().getResultado()).isEqualTo(ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE);
        assertThat(ultimoGuardado().getMensaje()).isEqualTo(SincronizacionService.MENSAJE_INTERRUMPIDA);
    }

    @Test
    void laVerificacionSinPendientesNoRegistraUnIntentoSoloLaFecha() {
        when(verificarVersionesPendientesUseCase.hayVersionesPendientes()).thenReturn(false);

        service.verificar();

        verify(ultimaVerificacionRepository).save(any(Instant.class));
        assertThat(guardados).isEmpty();
        verifyNoInteractions(sincronizarCatalogoIncrementalUseCase, sincronizarCatalogoCompletoUseCase);
    }

    @Test
    void laVerificacionConPendientesRegistraLaIncremental() {
        when(verificarVersionesPendientesUseCase.hayVersionesPendientes()).thenReturn(true);
        hayCopiaLocal();
        when(sincronizarCatalogoIncrementalUseCase.sincronizar()).thenReturn(version(6L));

        service.verificar();

        assertThat(ultimoGuardado().getProceso()).isEqualTo(ProcesoSincronizacion.INCREMENTAL);
        assertThat(ultimoGuardado().getResultado()).isEqualTo(ResultadoSincronizacion.OK);
        verify(ultimaVerificacionRepository, never()).save(any());
    }

    @Test
    void siNoSePuedeVerificarIntentaLaIncrementalYRegistraElError() {
        when(verificarVersionesPendientesUseCase.hayVersionesPendientes()).thenThrow(NO_DISPONIBLE);
        hayCopiaLocal();
        when(sincronizarCatalogoIncrementalUseCase.sincronizar()).thenThrow(NO_DISPONIBLE);

        service.verificar();

        verify(sincronizarCatalogoIncrementalUseCase, times(3)).sincronizar();
        assertThat(ultimoGuardado().getResultado()).isEqualTo(ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE);
        verify(ultimaVerificacionRepository, never()).save(any());
    }

    @Test
    void laVerificacionSinCopiaLocalHaceLaCompleta() {
        when(verificarVersionesPendientesUseCase.hayVersionesPendientes()).thenReturn(true);
        when(versionAplicadaRepository.find()).thenReturn(Optional.empty());
        when(sincronizarCatalogoCompletoUseCase.sincronizar()).thenReturn(version(1L));

        service.verificar();

        assertThat(ultimoGuardado().getProceso()).isEqualTo(ProcesoSincronizacion.COMPLETA);
        assertThat(ultimoGuardado().getResultado()).isEqualTo(ResultadoSincronizacion.OK);
    }

    @Test
    void siHayOtraSincronizacionCorriendoLaVerificacionSeSaltea() throws InterruptedException {
        CountDownLatch empezo = new CountDownLatch(1);
        CountDownLatch terminar = new CountDownLatch(1);
        when(sincronizarCatalogoCompletoUseCase.sincronizar()).thenAnswer(invocacion -> {
            empezo.countDown();
            terminar.await(5, TimeUnit.SECONDS);
            return version(1L);
        });
        Thread completa = new Thread(service::sincronizar);
        completa.start();
        assertThat(empezo.await(5, TimeUnit.SECONDS)).isTrue();

        service.verificar();

        terminar.countDown();
        completa.join(5000);
        verifyNoInteractions(verificarVersionesPendientesUseCase, ultimaVerificacionRepository);
    }

    @Test
    void cerrarIntentosInterrumpidosLosCierraComoError() {
        IntentoSincronizacion colgado = IntentoSincronizacion.builder()
                .id(5L).inicio(Instant.parse("2026-10-27T10:00:00Z")).intentos(1).build();
        when(intentoSincronizacionRepository.findAbiertos()).thenReturn(List.of(colgado));

        service.cerrarIntentosInterrumpidos();

        IntentoSincronizacion fin = ultimoGuardado();
        assertThat(fin.getId()).isEqualTo(5L);
        assertThat(fin.getFin()).isNotNull();
        assertThat(fin.getResultado()).isEqualTo(ResultadoSincronizacion.ERROR);
        assertThat(fin.getMensaje()).isEqualTo(SincronizacionService.MENSAJE_REINICIO);
    }

    @Test
    void sinIntentosAbiertosNoGuardaNada() {
        when(intentoSincronizacionRepository.findAbiertos()).thenReturn(List.of());

        service.cerrarIntentosInterrumpidos();

        assertThat(guardados).isEmpty();
    }

    @Test
    void consultarEstadoDelegaEnElCasoDeUso() {
        ResumenSincronizacion resumen = ResumenSincronizacion.builder().estado(EstadoSincronizacion.AL_DIA).build();
        when(consultarEstadoSincronizacionUseCase.consultarEstado()).thenReturn(resumen);

        assertThat(service.consultarEstado()).isSameAs(resumen);
    }

    @Test
    void laIncrementalRegistraElProcesoYLaVersionAlcanzada() {
        hayCopiaLocal();
        when(sincronizarCatalogoIncrementalUseCase.sincronizar()).thenReturn(version(9L));

        assertThat(service.sincronizarIncremental()).hasValueSatisfying(v -> assertThat(v.getVersion()).isEqualTo(9L));

        IntentoSincronizacion fin = ultimoGuardado();
        assertThat(fin.getProceso()).isEqualTo(ProcesoSincronizacion.INCREMENTAL);
        assertThat(fin.getResultado()).isEqualTo(ResultadoSincronizacion.OK);
        assertThat(fin.getVersionAplicada()).isEqualTo(9L);
        assertThat(fin.getIntentos()).isEqualTo(1);
        assertThat(fin.getMensaje()).isNull();
        verify(sincronizarCatalogoCompletoUseCase, never()).sincronizar();
    }

    @Test
    void sinCopiaLocalLaIncrementalHaceLaCompleta() {
        when(versionAplicadaRepository.find()).thenReturn(Optional.empty());
        when(sincronizarCatalogoCompletoUseCase.sincronizar()).thenReturn(version(1L));

        service.sincronizarIncremental();

        verify(sincronizarCatalogoIncrementalUseCase, never()).sincronizar();
        assertThat(ultimoGuardado().getProceso()).isEqualTo(ProcesoSincronizacion.COMPLETA);
        assertThat(ultimoGuardado().getResultado()).isEqualTo(ResultadoSincronizacion.OK);
    }

    @Test
    void siRedisNoRespondeLaIncrementalReintentaYRegistraQueNoRespondio() {
        hayCopiaLocal();
        when(sincronizarCatalogoIncrementalUseCase.sincronizar()).thenThrow(NO_DISPONIBLE);

        assertThat(service.sincronizarIncremental()).isEmpty();

        verify(sincronizarCatalogoIncrementalUseCase, times(3)).sincronizar();
        IntentoSincronizacion fin = ultimoGuardado();
        assertThat(fin.getProceso()).isEqualTo(ProcesoSincronizacion.INCREMENTAL);
        assertThat(fin.getResultado()).isEqualTo(ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE);
        assertThat(fin.getMensaje()).isEqualTo("La cátedra no respondió después de 3 intentos");
    }

    @Test
    void siRedisRechazaLasCredencialesLaIncrementalNoReintenta() {
        hayCopiaLocal();
        when(sincronizarCatalogoIncrementalUseCase.sincronizar())
                .thenThrow(new CatedraRechazoException("Redis rechazó las credenciales", null));

        assertThat(service.sincronizarIncremental()).isEmpty();

        verify(sincronizarCatalogoIncrementalUseCase).sincronizar();
        assertThat(ultimoGuardado().getResultado()).isEqualTo(ResultadoSincronizacion.RECHAZO);
    }

    @Test
    void anteUnaDiscontinuidadRecuperaConUnSnapshotYSigueConLaIncremental() {
        hayCopiaLocal();
        when(sincronizarCatalogoIncrementalUseCase.sincronizar())
                .thenThrow(DISCONTINUIDAD)
                .thenReturn(version(8L));
        when(sincronizarCatalogoCompletoUseCase.sincronizar()).thenReturn(version(7L));

        assertThat(service.sincronizarIncremental()).hasValueSatisfying(v -> assertThat(v.getVersion()).isEqualTo(8L));

        InOrder orden = inOrder(sincronizarCatalogoIncrementalUseCase, sincronizarCatalogoCompletoUseCase);
        orden.verify(sincronizarCatalogoIncrementalUseCase).sincronizar();
        orden.verify(sincronizarCatalogoCompletoUseCase).sincronizar();
        orden.verify(sincronizarCatalogoIncrementalUseCase).sincronizar();
        IntentoSincronizacion fin = ultimoGuardado();
        assertThat(fin.getProceso()).isEqualTo(ProcesoSincronizacion.RECUPERACION);
        assertThat(fin.getResultado()).isEqualTo(ResultadoSincronizacion.OK);
        assertThat(fin.getVersionAplicada()).isEqualTo(8L);
        assertThat(fin.getIntentos()).isEqualTo(3);
        assertThat(fin.getMensaje()).isEqualTo(DISCONTINUIDAD.getMessage());
    }

    @Test
    void siVuelveAHaberDiscontinuidadDespuesDelSnapshotNoReconstruyeOtraVez() {
        hayCopiaLocal();
        when(sincronizarCatalogoIncrementalUseCase.sincronizar())
                .thenThrow(DISCONTINUIDAD)
                .thenThrow(new DiscontinuidadException("Faltan los cambios de la versión 8"));
        when(sincronizarCatalogoCompletoUseCase.sincronizar()).thenReturn(version(7L));

        assertThat(service.sincronizarIncremental()).isEmpty();

        // D-08: una sola reconstrucción por disparador.
        verify(sincronizarCatalogoCompletoUseCase, times(1)).sincronizar();
        verify(sincronizarCatalogoIncrementalUseCase, times(2)).sincronizar();
        IntentoSincronizacion fin = ultimoGuardado();
        assertThat(fin.getProceso()).isEqualTo(ProcesoSincronizacion.RECUPERACION);
        assertThat(fin.getResultado()).isEqualTo(ResultadoSincronizacion.DISCONTINUIDAD);
        assertThat(fin.getVersionAplicada()).isEqualTo(7L);
        assertThat(fin.getMensaje()).isEqualTo("Faltan los cambios de la versión 8");
    }

    @Test
    void siFallaElSnapshotDeLaRecuperacionNoSigueConLaIncremental() {
        hayCopiaLocal();
        when(sincronizarCatalogoIncrementalUseCase.sincronizar()).thenThrow(DISCONTINUIDAD);
        when(sincronizarCatalogoCompletoUseCase.sincronizar()).thenThrow(NO_DISPONIBLE);

        assertThat(service.sincronizarIncremental()).isEmpty();

        verify(sincronizarCatalogoCompletoUseCase, times(3)).sincronizar();
        verify(sincronizarCatalogoIncrementalUseCase, times(1)).sincronizar();
        IntentoSincronizacion fin = ultimoGuardado();
        assertThat(fin.getProceso()).isEqualTo(ProcesoSincronizacion.RECUPERACION);
        assertThat(fin.getResultado()).isEqualTo(ResultadoSincronizacion.CATEDRA_NO_DISPONIBLE);
        assertThat(fin.getVersionAplicada()).isNull();
    }

    private void hayCopiaLocal() {
        when(versionAplicadaRepository.find()).thenReturn(Optional.of(version(5L)));
    }

    private static VersionAplicada version(Long numero) {
        return VersionAplicada.builder().version(numero).aplicadaEn(Instant.now()).build();
    }

    private IntentoSincronizacion ultimoGuardado() {
        return guardados.get(guardados.size() - 1);
    }

    private static IntentoSincronizacion copia(IntentoSincronizacion intento) {
        return IntentoSincronizacion.builder()
                .id(intento.getId())
                .proceso(intento.getProceso())
                .inicio(intento.getInicio())
                .fin(intento.getFin())
                .resultado(intento.getResultado())
                .intentos(intento.getIntentos())
                .versionAplicada(intento.getVersionAplicada())
                .mensaje(intento.getMensaje())
                .build();
    }
}
