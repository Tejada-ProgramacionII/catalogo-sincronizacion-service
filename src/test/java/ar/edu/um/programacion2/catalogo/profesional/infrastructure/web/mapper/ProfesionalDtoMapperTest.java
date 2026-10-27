package ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.dto.PaginaResponse;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.dto.ProfesionalResponse;

class ProfesionalDtoMapperTest {

    private final ProfesionalDtoMapper mapper = new ProfesionalDtoMapper();

    @Test
    void toFiltroCopiaLosFiltros() {
        LocalDate fecha = LocalDate.of(2026, 9, 14);

        FiltroBusquedaProfesionales filtro = mapper.toFiltro(1L, "ana", true, fecha);

        assertThat(filtro.getCategoriaId()).isEqualTo(1L);
        assertThat(filtro.getNombre()).isEqualTo("ana");
        assertThat(filtro.getHabilitado()).isTrue();
        assertThat(filtro.getFecha()).isEqualTo(fecha);
    }

    @Test
    void toResponseCopiaLosDatosPublicosDelProfesional() {
        Profesional profesional = Profesional.builder()
                .id(15L)
                .categoriaId(1L)
                .nombre("Ana")
                .apellido("Gomez")
                .habilitado(true)
                .creadoEn(Instant.parse("2026-07-01T12:00:00Z"))
                .actualizadoEn(Instant.parse("2026-09-10T13:40:00Z"))
                .build();

        ProfesionalResponse response = mapper.toResponse(profesional);

        assertThat(response).usingRecursiveComparison().isEqualTo(profesional);
    }

    @Test
    void toPaginaResponseConvierteElContenidoYLosDatosDePaginacion() {
        Pagina<Profesional> pagina = Pagina.<Profesional>builder()
                .contenido(List.of(Profesional.builder().id(15L).build(), Profesional.builder().id(16L).build()))
                .numero(1)
                .tamanio(2)
                .totalElementos(4)
                .totalPaginas(2)
                .build();

        PaginaResponse<ProfesionalResponse> response = mapper.toPaginaResponse(pagina);

        assertThat(response.getContenido()).extracting(ProfesionalResponse::getId).containsExactly(15L, 16L);
        assertThat(response.getNumero()).isEqualTo(1);
        assertThat(response.getTamanio()).isEqualTo(2);
        assertThat(response.getTotalElementos()).isEqualTo(4);
        assertThat(response.getTotalPaginas()).isEqualTo(2);
    }

    @Test
    void devuelveNullSiRecibeNull() {
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toPaginaResponse(null)).isNull();
    }
}
