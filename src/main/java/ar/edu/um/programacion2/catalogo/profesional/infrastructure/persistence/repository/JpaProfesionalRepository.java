package ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.entity.ProfesionalEntity;

@Repository
public interface JpaProfesionalRepository extends JpaRepository<ProfesionalEntity, Long> {

    // Cada filtro en null no filtra. La disponibilidad sigue D-09.
    @Query("""
            SELECT p FROM ProfesionalEntity p
            WHERE (:categoriaId IS NULL OR p.categoriaId = :categoriaId)
              AND (:habilitado IS NULL OR p.habilitado = :habilitado)
              AND (:nombre IS NULL OR LOWER(p.nombre) LIKE :nombre OR LOWER(p.apellido) LIKE :nombre)
              AND (:diaSemana IS NULL OR (p.habilitado = true AND EXISTS (
                    SELECT 1 FROM HorarioSemanalEntity h
                    WHERE h.profesionalId = p.id
                      AND h.habilitado = true
                      AND h.diaSemana = :diaSemana)))
            """)
    Page<ProfesionalEntity> buscar(
            @Param("categoriaId") Long categoriaId,
            @Param("habilitado") Boolean habilitado,
            @Param("nombre") String nombre,
            @Param("diaSemana") String diaSemana,
            Pageable pageable);
}
