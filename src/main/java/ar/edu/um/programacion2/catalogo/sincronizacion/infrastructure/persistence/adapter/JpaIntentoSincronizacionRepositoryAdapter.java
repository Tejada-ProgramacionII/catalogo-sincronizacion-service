package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.ports.out.IntentoSincronizacionRepository;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.IntentoSincronizacionEntity;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.mapper.IntentoSincronizacionMapper;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.repository.JpaIntentoSincronizacionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaIntentoSincronizacionRepositoryAdapter implements IntentoSincronizacionRepository {

    private final JpaIntentoSincronizacionRepository jpaIntentoSincronizacionRepository;
    private final IntentoSincronizacionMapper intentoSincronizacionMapper;

    @Override
    public IntentoSincronizacion save(IntentoSincronizacion intento) {
        IntentoSincronizacionEntity guardado = jpaIntentoSincronizacionRepository.save(
                intentoSincronizacionMapper.toEntity(intento));
        return intentoSincronizacionMapper.toDomainModel(guardado);
    }

    @Override
    public Optional<IntentoSincronizacion> findUltimo() {
        return jpaIntentoSincronizacionRepository.findFirstByOrderByIdDesc()
                .map(intentoSincronizacionMapper::toDomainModel);
    }

    @Override
    public List<IntentoSincronizacion> findAbiertos() {
        return jpaIntentoSincronizacionRepository.findByFinIsNull().stream()
                .map(intentoSincronizacionMapper::toDomainModel)
                .collect(Collectors.toList());
    }
}
