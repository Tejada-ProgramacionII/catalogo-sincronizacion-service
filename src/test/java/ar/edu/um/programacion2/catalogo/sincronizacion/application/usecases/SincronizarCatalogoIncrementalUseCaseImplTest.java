package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionOperations;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.categoria.domain.ports.out.CategoriaRepository;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.domain.ports.out.HorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatalogoNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.DiscontinuidadException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.CambiosVersion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionesPublicadas;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.CatalogoPublicadoClient;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;

@ExtendWith(MockitoExtension.class)
class SincronizarCatalogoIncrementalUseCaseImplTest {

    private static final Categoria CATEGORIA_1 = Categoria.builder().id(1L).nombre("Clinica").build();
    private static final Profesional PROFESIONAL_15 = Profesional.builder().id(15L).categoriaId(1L).build();
    private static final HorarioSemanal HORARIO_21 = HorarioSemanal.builder().id(21L).profesionalId(15L).build();

    @Mock
    private CatalogoPublicadoClient catalogoPublicadoClient;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProfesionalRepository profesionalRepository;

    @Mock
    private HorarioSemanalRepository horarioSemanalRepository;

    @Mock
    private VersionAplicadaRepository versionAplicadaRepository;

    private SincronizarCatalogoIncrementalUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SincronizarCatalogoIncrementalUseCaseImpl(catalogoPublicadoClient, categoriaRepository,
                profesionalRepository, horarioSemanalRepository, versionAplicadaRepository,
                TransactionOperations.withoutTransaction());
    }

    @Test
    void siYaEstaAlDiaNoLeeCambiosNiEscribe() {
        dadas(7L, 7L, 4L);

        assertThat(useCase.sincronizar().getVersion()).isEqualTo(7L);

        verify(catalogoPublicadoClient, never()).getCambios(anyLong());
        verify(versionAplicadaRepository, never()).save(any());
        verifyNoInteractions(categoriaRepository, profesionalRepository, horarioSemanalRepository);
    }

    @Test
    void aplicaCadaVersionPendienteEnOrdenYAvanzaLaVersionDespuesDeSusCambios() {
        dadas(5L, 7L, 4L);
        cuandoLaVersion(6L, cambios(6L, List.of(1L), List.of(), List.of()));
        cuandoLaVersion(7L, cambios(7L, List.of(), List.of(15L), List.of(21L)));
        when(catalogoPublicadoClient.getCategorias(List.of(1L))).thenReturn(List.of(CATEGORIA_1));
        when(catalogoPublicadoClient.getCategorias(List.of())).thenReturn(List.of());
        when(catalogoPublicadoClient.getProfesionales(List.of())).thenReturn(List.of());
        when(catalogoPublicadoClient.getProfesionales(List.of(15L))).thenReturn(List.of(PROFESIONAL_15));
        when(catalogoPublicadoClient.getHorarios(List.of())).thenReturn(List.of());
        when(catalogoPublicadoClient.getHorarios(List.of(21L))).thenReturn(List.of(HORARIO_21));

        assertThat(useCase.sincronizar().getVersion()).isEqualTo(7L);

        InOrder orden = inOrder(categoriaRepository, profesionalRepository, horarioSemanalRepository,
                versionAplicadaRepository);
        orden.verify(categoriaRepository).save(CATEGORIA_1);
        orden.verify(versionAplicadaRepository).save(versionNumero(6L));
        orden.verify(profesionalRepository).save(PROFESIONAL_15);
        orden.verify(horarioSemanalRepository).save(HORARIO_21);
        orden.verify(versionAplicadaRepository).save(versionNumero(7L));
    }

    @Test
    void siLaVersionLocalQuedoAfueraDeLaVentanaEsDiscontinuidad() {
        dadas(3L, 7L, 4L);

        assertThatThrownBy(useCase::sincronizar)
                .isInstanceOf(DiscontinuidadException.class)
                .hasMessage("La versión local 3 es anterior a la más antigua disponible (4)");
        verify(catalogoPublicadoClient, never()).getCambios(anyLong());
    }

    @Test
    void siFaltanLosCambiosDeUnaVersionLasAnterioresQuedanAplicadas() {
        dadas(5L, 7L, 4L);
        cuandoLaVersion(6L, cambios(6L, List.of(), List.of(), List.of()));
        when(catalogoPublicadoClient.getCambios(7L)).thenReturn(Optional.empty());
        when(catalogoPublicadoClient.getCategorias(List.of())).thenReturn(List.of());
        when(catalogoPublicadoClient.getProfesionales(List.of())).thenReturn(List.of());
        when(catalogoPublicadoClient.getHorarios(List.of())).thenReturn(List.of());

        assertThatThrownBy(useCase::sincronizar)
                .isInstanceOf(DiscontinuidadException.class)
                .hasMessage("Faltan los cambios de la versión 7");

        ArgumentCaptor<VersionAplicada> guardada = ArgumentCaptor.forClass(VersionAplicada.class);
        verify(versionAplicadaRepository, times(1)).save(guardada.capture());
        assertThat(guardada.getValue().getVersion()).isEqualTo(6L);
    }

    @Test
    void siUnaEntidadCambiadaNoEstaPublicadaLaVersionNoTocaLaBase() {
        dadas(5L, 6L, 4L);
        cuandoLaVersion(6L, cambios(6L, List.of(1L), List.of(15L, 16L), List.of()));
        when(catalogoPublicadoClient.getCategorias(List.of(1L))).thenReturn(List.of(CATEGORIA_1));
        when(catalogoPublicadoClient.getProfesionales(List.of(15L, 16L))).thenReturn(List.of(PROFESIONAL_15));

        assertThatThrownBy(useCase::sincronizar)
                .isInstanceOf(DiscontinuidadException.class)
                .hasMessage("La versión 6 cambia entidades que no están publicadas");

        verifyNoInteractions(categoriaRepository, profesionalRepository, horarioSemanalRepository);
        verify(versionAplicadaRepository, never()).save(any());
    }

    @Test
    void sinCopiaLocalNoHayDesdeDondeSeguir() {
        when(versionAplicadaRepository.find()).thenReturn(Optional.empty());

        assertThatThrownBy(useCase::sincronizar).isInstanceOf(CatalogoNoDisponibleException.class);
        verifyNoInteractions(catalogoPublicadoClient);
    }

    private void dadas(Long local, Long actual, Long masAntigua) {
        when(versionAplicadaRepository.find()).thenReturn(
                Optional.of(VersionAplicada.builder().version(local).aplicadaEn(Instant.now()).build()));
        when(catalogoPublicadoClient.getVersiones()).thenReturn(
                VersionesPublicadas.builder().actual(actual).masAntiguaDisponible(masAntigua).build());
    }

    private void cuandoLaVersion(Long version, CambiosVersion cambios) {
        when(catalogoPublicadoClient.getCambios(version)).thenReturn(Optional.of(cambios));
    }

    private static CambiosVersion cambios(Long version, List<Long> categorias, List<Long> profesionales,
            List<Long> horarios) {
        return CambiosVersion.builder().version(version).categoriaIds(categorias)
                .profesionalIds(profesionales).horarioIds(horarios).build();
    }

    private static VersionAplicada versionNumero(Long version) {
        return argThat(aplicada -> aplicada.getVersion().equals(version));
    }
}
