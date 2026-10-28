package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.UltimaVerificacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.UltimaVerificacionEntity;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository.JpaUltimaVerificacionRepository;
import lombok.RequiredArgsConstructor;

// Sin mapper: el dato de dominio es un Instant, no hay modelo que traducir.
@Component
@RequiredArgsConstructor
public class JpaUltimaVerificacionRepositoryAdapter implements UltimaVerificacionRepository {

    private final JpaUltimaVerificacionRepository jpaUltimaVerificacionRepository;

    @Override
    public Optional<Instant> find() {
        return jpaUltimaVerificacionRepository.findById(UltimaVerificacionEntity.ID_UNICO)
                .map(UltimaVerificacionEntity::getVerificadaEn);
    }

    @Override
    public void save(Instant verificadaEn) {
        jpaUltimaVerificacionRepository.save(UltimaVerificacionEntity.builder()
                .id(UltimaVerificacionEntity.ID_UNICO)
                .verificadaEn(verificadaEn)
                .build());
    }
}
