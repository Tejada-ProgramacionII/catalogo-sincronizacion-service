package ar.edu.um.programacion2.catalogo.profesional.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.programacion2.catalogo.profesional.application.exception.ProfesionalNoEncontradoException;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.AgendaProfesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.in.BuscarProfesionalesUseCase;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.in.ConsultarProfesionalUseCase;

@ExtendWith(MockitoExtension.class)
class ProfesionalServiceTest {

    @Mock
    private BuscarProfesionalesUseCase buscarProfesionalesUseCase;

    @Mock
    private ConsultarProfesionalUseCase consultarProfesionalUseCase;

    @InjectMocks
    private ProfesionalService service;

    @Test
    void buscarProfesionalesDelegaEnElCasoDeUso() {
        FiltroBusquedaProfesionales filtro = FiltroBusquedaProfesionales.builder().nombre("ana").build();
        Pagina<Profesional> pagina = Pagina.<Profesional>builder().contenido(List.of()).build();
        when(buscarProfesionalesUseCase.buscarProfesionales(filtro, 0, 20)).thenReturn(pagina);

        assertThat(service.buscarProfesionales(filtro, 0, 20)).isSameAs(pagina);
    }

    @Test
    void consultarProfesionalDevuelveLaAgendaDelCasoDeUso() {
        AgendaProfesional agenda = AgendaProfesional.builder().versionCatalogo(7L).build();
        when(consultarProfesionalUseCase.consultarProfesional(10L)).thenReturn(Optional.of(agenda));

        assertThat(service.consultarProfesional(10L)).isSameAs(agenda);
    }

    @Test
    void consultarProfesionalTiraExcepcionSiNoExiste() {
        when(consultarProfesionalUseCase.consultarProfesional(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarProfesional(99L))
                .isInstanceOf(ProfesionalNoEncontradoException.class)
                .hasMessage("No existe el profesional 99");
    }
}
