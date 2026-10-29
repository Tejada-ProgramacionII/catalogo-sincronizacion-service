package ar.edu.um.programacion2.catalogo.sincronizacion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.programacion2.catalogo.config.SincronizacionProperties;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraRechazoException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.in.SincronizarCatalogoCompletoUseCase;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;

@ExtendWith(MockitoExtension.class)
class SincronizacionServiceTest {

    private static final CatedraNoDisponibleException NO_DISPONIBLE =
            new CatedraNoDisponibleException("La cátedra respondió con un error (HTTP 503)", null);

    @Mock
    private SincronizarCatalogoCompletoUseCase sincronizarCatalogoCompletoUseCase;

    @Mock
    private VersionAplicadaRepository versionAplicadaRepository;

    private SincronizacionProperties sincronizacionProperties;

    private SincronizacionService service;

    @BeforeEach
    void setUp() {
        sincronizacionProperties = new SincronizacionProperties();
        sincronizacionProperties.setMaxIntentos(3);
        sincronizacionProperties.setEsperaEntreIntentos(Duration.ZERO);
        service = new SincronizacionService(sincronizarCatalogoCompletoUseCase, sincronizacionProperties,
                versionAplicadaRepository);
    }

    @Test
    void reintentaHastaQueLaCatedraResponde() {
        VersionAplicada version = VersionAplicada.builder().version(7L).aplicadaEn(Instant.now()).build();
        when(sincronizarCatalogoCompletoUseCase.sincronizar())
                .thenThrow(NO_DISPONIBLE)
                .thenThrow(NO_DISPONIBLE)
                .thenReturn(version);

        assertThat(service.sincronizar()).contains(version);
        verify(sincronizarCatalogoCompletoUseCase, times(3)).sincronizar();
    }

    @Test
    void siLaCatedraFallaSiempreHaceMaxIntentosYNoPropaga() {
        when(sincronizarCatalogoCompletoUseCase.sincronizar()).thenThrow(NO_DISPONIBLE);

        assertThat(service.sincronizar()).isEmpty();
        verify(sincronizarCatalogoCompletoUseCase, times(3)).sincronizar();
    }

    @Test
    void siLaCatedraRechazaElPedidoNoReintenta() {
        when(sincronizarCatalogoCompletoUseCase.sincronizar())
                .thenThrow(new CatedraRechazoException("La cátedra rechazó el pedido (HTTP 401)", null));

        assertThat(service.sincronizar()).isEmpty();
        verify(sincronizarCatalogoCompletoUseCase).sincronizar();
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
    }

    @Test
    void sinCopiaLocalSincroniza() {
        when(versionAplicadaRepository.find()).thenReturn(Optional.empty());
        when(sincronizarCatalogoCompletoUseCase.sincronizar())
                .thenReturn(VersionAplicada.builder().version(7L).aplicadaEn(Instant.now()).build());

        service.sincronizarSiNoHayCopia();

        verify(sincronizarCatalogoCompletoUseCase).sincronizar();
    }

    @Test
    void conCopiaLocalNoSincroniza() {
        when(versionAplicadaRepository.find())
                .thenReturn(Optional.of(VersionAplicada.builder().version(7L).aplicadaEn(Instant.now()).build()));

        service.sincronizarSiNoHayCopia();

        verify(sincronizarCatalogoCompletoUseCase, never()).sincronizar();
    }
}
