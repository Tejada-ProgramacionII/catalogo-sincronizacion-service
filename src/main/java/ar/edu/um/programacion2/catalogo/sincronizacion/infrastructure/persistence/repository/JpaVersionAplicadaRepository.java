package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.VersionAplicadaEntity;

@Repository
public interface JpaVersionAplicadaRepository extends JpaRepository<VersionAplicadaEntity, Long> {
}
