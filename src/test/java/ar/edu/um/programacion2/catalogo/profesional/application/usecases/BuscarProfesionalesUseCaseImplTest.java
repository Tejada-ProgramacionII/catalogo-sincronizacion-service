package ar.edu.um.programacion2.catalogo.profesional.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.categoria.domain.ports.out.CategoriaRepository;
import ar.edu.um.programacion2.catalogo.profesional.application.exception.FiltroBusquedaInvalidoException;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatalogoNoDisponibleException;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;

@ExtendWith(MockitoExtension.class)
class BuscarProfesionalesUseCaseImplTest {

    private static final VersionAplicada VERSION = VersionAplicada.builder()
            .version(7L)
            .aplicadaEn(Instant.parse("2026-09-10T13:45:00Z"))
            .build();

    @Mock
    private ProfesionalRepository profesionalRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private VersionAplicadaRepository versionAplicadaRepository;

    @InjectMocks
    private BuscarProfesionalesUseCaseImpl useCase;

    @Test
    void sinVersionAplicadaElCatalogoNoEstaDisponible() {
        when(versionAplicadaRepository.find()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.buscarProfesionales(new FiltroBusquedaProfesionales(), 0, 20))
                .isInstanceOf(CatalogoNoDisponibleException.class);
        verify(profesionalRepository, never()).search(any(), anyInt(), anyInt());
    }

    @Test
    void unaCategoriaInexistenteEsUnFiltroInvalido() {
        when(versionAplicadaRepository.find()).thenReturn(Optional.of(VERSION));
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());
        FiltroBusquedaProfesionales filtro = FiltroBusquedaProfesionales.builder().categoriaId(99L).build();

        assertThatThrownBy(() -> useCase.buscarProfesionales(filtro, 0, 20))
                .isInstanceOf(FiltroBusquedaInvalidoException.class)
                .hasMessageContaining("99");
        verify(profesionalRepository, never()).search(any(), anyInt(), anyInt());
    }

    @Test
    void conUnaCategoriaExistenteBuscaEnLaCopiaLocal() {
        when(versionAplicadaRepository.find()).thenReturn(Optional.of(VERSION));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(Categoria.builder().id(1L).build()));
        FiltroBusquedaProfesionales filtro = FiltroBusquedaProfesionales.builder().categoriaId(1L).build();
        Pagina<Profesional> pagina = paginaCon(Profesional.builder().id(15L).build());
        when(profesionalRepository.search(filtro, 0, 20)).thenReturn(pagina);

        assertThat(useCase.buscarProfesionales(filtro, 0, 20)).isSameAs(pagina);
    }

    @Test
    void sinFiltroDeCategoriaNoLaValida() {
        when(versionAplicadaRepository.find()).thenReturn(Optional.of(VERSION));
        FiltroBusquedaProfesionales filtro = new FiltroBusquedaProfesionales();
        Pagina<Profesional> pagina = paginaCon();
        when(profesionalRepository.search(filtro, 1, 10)).thenReturn(pagina);

        assertThat(useCase.buscarProfesionales(filtro, 1, 10)).isSameAs(pagina);
        verify(categoriaRepository, never()).findById(anyLong());
    }

    private static Pagina<Profesional> paginaCon(Profesional... profesionales) {
        return Pagina.<Profesional>builder()
                .contenido(List.of(profesionales))
                .totalElementos(profesionales.length)
                .build();
    }
}
