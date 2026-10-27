package ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.entity.HorarioSemanalEntity;

@Repository
public interface JpaHorarioSemanalRepository extends JpaRepository<HorarioSemanalEntity, Long> {
    List<HorarioSemanalEntity> findByProfesionalIdAndHabilitadoTrue(Long profesionalId);
}
