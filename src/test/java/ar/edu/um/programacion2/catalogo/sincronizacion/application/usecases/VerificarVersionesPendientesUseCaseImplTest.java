package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionesPublicadas;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.CatalogoPublicadoClient;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;

@ExtendWith(MockitoExtension.class)
class VerificarVersionesPendientesUseCaseImplTest {

    @Mock
    private VersionAplicadaRepository versionAplicadaRepository;

    @Mock
    private CatalogoPublicadoClient catalogoPublicadoClient;

    @InjectMocks
    private VerificarVersionesPendientesUseCaseImpl useCase;

    @Test
    void sinCopiaLocalHayPendientesSinPreguntarARedis() {
        when(versionAplicadaRepository.find()).thenReturn(Optional.empty());

        assertThat(useCase.hayVersionesPendientes()).isTrue();
        verifyNoInteractions(catalogoPublicadoClient);
    }

    @Test
    void conUnaVersionPublicadaMasNuevaHayPendientes() {
        dadas(1L, 2L);

        assertThat(useCase.hayVersionesPendientes()).isTrue();
    }

    @Test
    void conLaMismaVersionNoHayPendientes() {
        dadas(1L, 1L);

        assertThat(useCase.hayVersionesPendientes()).isFalse();
    }

    private void dadas(Long local, Long actual) {
        when(versionAplicadaRepository.find()).thenReturn(
                Optional.of(VersionAplicada.builder().version(local).aplicadaEn(Instant.now()).build()));
        when(catalogoPublicadoClient.getVersiones())
                .thenReturn(VersionesPublicadas.builder().actual(actual).masAntiguaDisponible(1L).build());
    }
}
