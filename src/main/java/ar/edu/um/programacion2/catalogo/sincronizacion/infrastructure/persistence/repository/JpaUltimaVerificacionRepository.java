package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.UltimaVerificacionEntity;

@Repository
public interface JpaUltimaVerificacionRepository extends JpaRepository<UltimaVerificacionEntity, Long> {
}
