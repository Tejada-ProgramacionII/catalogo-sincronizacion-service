package ar.edu.um.programacion2.catalogo.sincronizacion.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.categoria.domain.ports.out.CategoriaRepository;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.domain.ports.out.HorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatedraNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.SnapshotCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.CatalogoCatedraClient;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;

@ExtendWith(MockitoExtension.class)
class SincronizarCatalogoCompletoUseCaseImplTest {

    @Mock
    private CatalogoCatedraClient catalogoCatedraClient;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProfesionalRepository profesionalRepository;

    @Mock
    private HorarioSemanalRepository horarioSemanalRepository;

    @Mock
    private VersionAplicadaRepository versionAplicadaRepository;

    @InjectMocks
    private SincronizarCatalogoCompletoUseCaseImpl useCase;

    @Test
    void borraEnOrdenInversoALasClavesForaneasYGuardaEnOrdenDirecto() {
        Categoria categoria = Categoria.builder().id(1L).build();
        Profesional profesional = Profesional.builder().id(10L).categoriaId(1L).build();
        HorarioSemanal horario = HorarioSemanal.builder().id(100L).profesionalId(10L).build();
        when(catalogoCatedraClient.getSnapshot()).thenReturn(SnapshotCatalogo.builder()
                .version(7L)
                .categorias(List.of(categoria))
                .profesionales(List.of(profesional))
                .horarios(List.of(horario))
                .build());
        when(versionAplicadaRepository.save(any(VersionAplicada.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        VersionAplicada aplicada = useCase.sincronizar();

        assertThat(aplicada.getVersion()).isEqualTo(7L);
        assertThat(aplicada.getAplicadaEn()).isNotNull();
        InOrder orden = inOrder(catalogoCatedraClient, horarioSemanalRepository, profesionalRepository,
                categoriaRepository, versionAplicadaRepository);
        orden.verify(catalogoCatedraClient).getSnapshot();
        orden.verify(horarioSemanalRepository).deleteAll();
        orden.verify(profesionalRepository).deleteAll();
        orden.verify(categoriaRepository).deleteAll();
        orden.verify(categoriaRepository).save(categoria);
        orden.verify(profesionalRepository).save(profesional);
        orden.verify(horarioSemanalRepository).save(horario);
        orden.verify(versionAplicadaRepository).save(any(VersionAplicada.class));
    }

    @Test
    void siLaCatedraFallaNoTocaNingunRepositorio() {
        when(catalogoCatedraClient.getSnapshot())
                .thenThrow(new CatedraNoDisponibleException("La cátedra no respondió", null));

        assertThatThrownBy(useCase::sincronizar).isInstanceOf(CatedraNoDisponibleException.class);

        verifyNoInteractions(categoriaRepository, profesionalRepository, horarioSemanalRepository,
                versionAplicadaRepository);
    }
}
