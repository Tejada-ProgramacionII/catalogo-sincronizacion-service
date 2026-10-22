package ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.entity.ProfesionalEntity;

@Component
public class ProfesionalMapper {

    public ProfesionalEntity toEntity(Profesional profesional) {
        if (profesional == null) {
            return null;
        }
        return ProfesionalEntity.builder()
                .id(profesional.getId())
                .categoriaId(profesional.getCategoriaId())
                .nombre(profesional.getNombre())
                .apellido(profesional.getApellido())
                .habilitado(profesional.isHabilitado())
                .creadoEn(profesional.getCreadoEn())
                .actualizadoEn(profesional.getActualizadoEn())
                .build();
    }

    public Profesional toDomainModel(ProfesionalEntity entity) {
        if (entity == null) {
            return null;
        }
        return Profesional.builder()
                .id(entity.getId())
                .categoriaId(entity.getCategoriaId())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .habilitado(entity.isHabilitado())
                .creadoEn(entity.getCreadoEn())
                .actualizadoEn(entity.getActualizadoEn())
                .build();
    }
}
