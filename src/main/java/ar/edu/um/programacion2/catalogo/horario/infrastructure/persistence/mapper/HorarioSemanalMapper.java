package ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.mapper;

import java.time.DayOfWeek;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.entity.HorarioSemanalEntity;

@Component
public class HorarioSemanalMapper {

    public HorarioSemanalEntity toEntity(HorarioSemanal horario) {
        if (horario == null) {
            return null;
        }
        return HorarioSemanalEntity.builder()
                .id(horario.getId())
                .profesionalId(horario.getProfesionalId())
                .diaSemana(horario.getDiaSemana().name())
                .horaInicio(horario.getHoraInicio())
                .horaFin(horario.getHoraFin())
                .duracionTurnoMinutos(horario.getDuracionTurnoMinutos())
                .habilitado(horario.isHabilitado())
                .creadoEn(horario.getCreadoEn())
                .actualizadoEn(horario.getActualizadoEn())
                .build();
    }

    public HorarioSemanal toDomainModel(HorarioSemanalEntity entity) {
        if (entity == null) {
            return null;
        }
        return HorarioSemanal.builder()
                .id(entity.getId())
                .profesionalId(entity.getProfesionalId())
                .diaSemana(DayOfWeek.valueOf(entity.getDiaSemana()))
                .horaInicio(entity.getHoraInicio())
                .horaFin(entity.getHoraFin())
                .duracionTurnoMinutos(entity.getDuracionTurnoMinutos())
                .habilitado(entity.isHabilitado())
                .creadoEn(entity.getCreadoEn())
                .actualizadoEn(entity.getActualizadoEn())
                .build();
    }
}
