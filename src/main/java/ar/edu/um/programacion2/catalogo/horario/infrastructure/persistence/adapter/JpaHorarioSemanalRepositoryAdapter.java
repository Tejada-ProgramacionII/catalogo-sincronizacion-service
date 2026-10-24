package ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.horario.domain.model.HorarioSemanal;
import ar.edu.um.programacion2.catalogo.horario.domain.ports.out.HorarioSemanalRepository;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.entity.HorarioSemanalEntity;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.mapper.HorarioSemanalMapper;
import ar.edu.um.programacion2.catalogo.horario.infrastructure.persistence.repository.JpaHorarioSemanalRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaHorarioSemanalRepositoryAdapter implements HorarioSemanalRepository {

    private final JpaHorarioSemanalRepository jpaHorarioSemanalRepository;
    private final HorarioSemanalMapper horarioSemanalMapper;

    @Override
    public HorarioSemanal save(HorarioSemanal horarioSemanal) {
        HorarioSemanalEntity guardado = jpaHorarioSemanalRepository.save(horarioSemanalMapper.toEntity(horarioSemanal));
        return horarioSemanalMapper.toDomainModel(guardado);
    }

    @Override
    public Optional<HorarioSemanal> findById(Long id) {
        return jpaHorarioSemanalRepository.findById(id).map(horarioSemanalMapper::toDomainModel);
    }

    @Override
    public List<HorarioSemanal> findAll() {
        return jpaHorarioSemanalRepository.findAll().stream()
                .map(horarioSemanalMapper::toDomainModel)
                .collect(Collectors.toList());
    }
}
