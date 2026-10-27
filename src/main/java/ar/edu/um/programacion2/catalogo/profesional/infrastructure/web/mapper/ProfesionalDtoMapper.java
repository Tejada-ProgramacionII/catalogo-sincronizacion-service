package ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.mapper;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.AgendaProfesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Pagina;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.dto.AgendaProfesionalResponse;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.dto.HorarioSemanalResponse;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.dto.PaginaResponse;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.dto.ProfesionalResponse;

@Component
public class ProfesionalDtoMapper {

    public FiltroBusquedaProfesionales toFiltro(Long categoriaId, String nombre, Boolean habilitado, LocalDate fecha) {
        return FiltroBusquedaProfesionales.builder()
                .categoriaId(categoriaId)
                .nombre(nombre)
                .habilitado(habilitado)
                .fecha(fecha)
                .build();
    }

    public ProfesionalResponse toResponse(Profesional profesional) {
        if (profesional == null) {
            return null;
        }
        return ProfesionalResponse.builder()
                .id(profesional.getId())
                .categoriaId(profesional.getCategoriaId())
                .nombre(profesional.getNombre())
                .apellido(profesional.getApellido())
                .habilitado(profesional.isHabilitado())
                .build();
    }

    public PaginaResponse<ProfesionalResponse> toPaginaResponse(Pagina<Profesional> pagina) {
        if (pagina == null) {
            return null;
        }
        return PaginaResponse.<ProfesionalResponse>builder()
                .contenido(pagina.getContenido().stream().map(this::toResponse).toList())
                .numero(pagina.getNumero())
                .tamanio(pagina.getTamanio())
                .totalElementos(pagina.getTotalElementos())
                .totalPaginas(pagina.getTotalPaginas())
                .build();
    }

    public AgendaProfesionalResponse toAgendaResponse(AgendaProfesional agenda) {
        if (agenda == null) {
            return null;
        }
        Profesional profesional = agenda.getProfesional();
        return AgendaProfesionalResponse.builder()
                .id(profesional.getId())
                .categoriaId(profesional.getCategoriaId())
                .nombre(profesional.getNombre())
                .apellido(profesional.getApellido())
                .habilitado(profesional.isHabilitado())
                .horarios(agenda.getHorarios().stream().map(this::toHorarioResponse).toList())
                .versionCatalogo(agenda.getVersionCatalogo())
                .build();
    }

    private HorarioSemanalResponse toHorarioResponse(HorarioSemanal horario) {
        return HorarioSemanalResponse.builder()
                .diaSemana(horario.getDiaSemana())
                .horaInicio(horario.getHoraInicio())
                .horaFin(horario.getHoraFin())
                .duracionTurnoMinutos(horario.getDuracionTurnoMinutos())
                .build();
    }
}
