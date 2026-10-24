package ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.categoria.domain.model.Categoria;
import ar.edu.um.programacion2.catalogo.categoria.infrastructure.persistence.entity.CategoriaEntity;

@Component
public class CategoriaMapper {

    public CategoriaEntity toEntity(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return CategoriaEntity.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .habilitada(categoria.isHabilitada())
                .creadaEn(categoria.getCreadaEn())
                .actualizadaEn(categoria.getActualizadaEn())
                .build();
    }

    public Categoria toDomainModel(CategoriaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Categoria.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .habilitada(entity.isHabilitada())
                .creadaEn(entity.getCreadaEn())
                .actualizadaEn(entity.getActualizadaEn())
                .build();
    }
}
