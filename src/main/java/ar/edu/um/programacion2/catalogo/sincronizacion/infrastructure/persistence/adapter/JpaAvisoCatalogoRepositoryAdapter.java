package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.AvisoCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.AvisoCatalogoRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.mapper.AvisoCatalogoMapper;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository.JpaAvisoCatalogoRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaAvisoCatalogoRepositoryAdapter implements AvisoCatalogoRepository {

    private final JpaAvisoCatalogoRepository jpaAvisoCatalogoRepository;
    private final AvisoCatalogoMapper avisoCatalogoMapper;

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaAvisoCatalogoRepository.existsById(eventId);
    }

    @Override
    public void save(AvisoCatalogo aviso) {
        jpaAvisoCatalogoRepository.save(avisoCatalogoMapper.toEntity(aviso));
    }
}
