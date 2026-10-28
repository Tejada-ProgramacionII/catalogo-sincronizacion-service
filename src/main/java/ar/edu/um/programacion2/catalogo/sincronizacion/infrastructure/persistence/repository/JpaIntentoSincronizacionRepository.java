package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.IntentoSincronizacionEntity;

@Repository
public interface JpaIntentoSincronizacionRepository extends JpaRepository<IntentoSincronizacionEntity, Long> {
    Optional<IntentoSincronizacionEntity> findFirstByOrderByIdDesc();

    List<IntentoSincronizacionEntity> findByFinIsNull();
}
