package ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.entity.ProfesionalEntity;

@Repository
public interface JpaProfesionalRepository extends JpaRepository<ProfesionalEntity, Long> {
}
