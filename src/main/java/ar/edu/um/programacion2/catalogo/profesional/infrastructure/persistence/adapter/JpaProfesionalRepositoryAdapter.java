package ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.profesional.domain.model.Profesional;
import ar.edu.um.programacion2.catalogo.profesional.domain.ports.out.ProfesionalRepository;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.entity.ProfesionalEntity;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.mapper.ProfesionalMapper;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.persistence.repository.JpaProfesionalRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaProfesionalRepositoryAdapter implements ProfesionalRepository {

    private final JpaProfesionalRepository jpaProfesionalRepository;
    private final ProfesionalMapper profesionalMapper;

    @Override
    public Profesional save(Profesional profesional) {
        ProfesionalEntity guardado = jpaProfesionalRepository.save(profesionalMapper.toEntity(profesional));
        return profesionalMapper.toDomainModel(guardado);
    }

    @Override
    public Optional<Profesional> findById(Long id) {
        return jpaProfesionalRepository.findById(id).map(profesionalMapper::toDomainModel);
    }

    @Override
    public List<Profesional> findAll() {
        return jpaProfesionalRepository.findAll().stream()
                .map(profesionalMapper::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        jpaProfesionalRepository.deleteAllInBatch();
    }
}
