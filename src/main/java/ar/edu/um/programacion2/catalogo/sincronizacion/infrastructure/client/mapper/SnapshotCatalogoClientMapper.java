package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.mapper;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.SnapshotCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.CategoriaCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.HorarioSemanalCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.ProfesionalCatedraResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto.SnapshotCatalogoResponse;

// Traduce el JSON de la cátedra al dominio: lo usan el snapshot REST y los Hashes de Redis.
@Component
public class SnapshotCatalogoClientMapper {

    public SnapshotCatalogo toDomainModel(SnapshotCatalogoResponse response) {
        if (response == null) {
            return null;
        }
        return SnapshotCatalogo.builder()
                .version(response.getSnapshotVersion())
                .generadoEn(response.getGeneratedAt())
                .categorias(response.getProfessionalCategories().stream().map(this::toCategoria).toList())
                .profesionales(response.getProfessionals().stream().map(this::toProfesional).toList())
                .horarios(response.getWeeklySchedules().stream().map(this::toHorario).toList())
                .build();
    }

    public Categoria toCategoria(CategoriaCatedraResponse response) {
        return Categoria.builder()
                .id(response.getId())
                .nombre(response.getName())
                .descripcion(response.getDescription())
                .habilitada(response.isEnabled())
                .creadaEn(response.getCreatedAt())
                .actualizadaEn(response.getUpdatedAt())
                .build();
    }

    public Profesional toProfesional(ProfesionalCatedraResponse response) {
        return Profesional.builder()
                .id(response.getId())
                .categoriaId(response.getCategoryId())
                .nombre(response.getFirstName())
                .apellido(response.getLastName())
                .habilitado(response.isEnabled())
                .creadoEn(response.getCreatedAt())
                .actualizadoEn(response.getUpdatedAt())
                .build();
    }

    public HorarioSemanal toHorario(HorarioSemanalCatedraResponse response) {
        return HorarioSemanal.builder()
                .id(response.getId())
                .profesionalId(response.getProfessionalId())
                .diaSemana(response.getDayOfWeek())
                .horaInicio(response.getStartTime())
                .horaFin(response.getEndTime())
                .duracionTurnoMinutos(response.getSlotDurationMinutes())
                .habilitado(response.isEnabled())
                .creadoEn(response.getCreatedAt())
                .actualizadoEn(response.getUpdatedAt())
                .build();
    }
}
