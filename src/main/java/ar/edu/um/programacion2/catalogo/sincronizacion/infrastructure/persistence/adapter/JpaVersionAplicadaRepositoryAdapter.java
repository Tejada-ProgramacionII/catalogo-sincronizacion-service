package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.VersionAplicadaRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.VersionAplicadaEntity;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.mapper.VersionAplicadaMapper;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository.JpaVersionAplicadaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaVersionAplicadaRepositoryAdapter implements VersionAplicadaRepository {

    private final JpaVersionAplicadaRepository jpaVersionAplicadaRepository;
    private final VersionAplicadaMapper versionAplicadaMapper;

    @Override
    public Optional<VersionAplicada> find() {
        return jpaVersionAplicadaRepository.findById(VersionAplicadaEntity.ID_UNICO)
                .map(versionAplicadaMapper::toDomainModel);
    }

    @Override
    public VersionAplicada save(VersionAplicada versionAplicada) {
        VersionAplicadaEntity guardada = jpaVersionAplicadaRepository.save(versionAplicadaMapper.toEntity(versionAplicada));
        return versionAplicadaMapper.toDomainModel(guardada);
    }
}
