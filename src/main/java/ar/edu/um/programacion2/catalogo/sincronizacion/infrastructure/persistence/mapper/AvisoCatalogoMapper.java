package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.AvisoCatalogo;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.AvisoCatalogoEntity;

@Component
public class AvisoCatalogoMapper {

    public AvisoCatalogoEntity toEntity(AvisoCatalogo aviso) {
        if (aviso == null) {
            return null;
        }
        return AvisoCatalogoEntity.builder()
                .eventId(aviso.getEventId())
                .version(aviso.getVersion())
                .procesadoEn(aviso.getProcesadoEn())
                .build();
    }

    public AvisoCatalogo toDomainModel(AvisoCatalogoEntity entity) {
        if (entity == null) {
            return null;
        }
        return AvisoCatalogo.builder()
                .eventId(entity.getEventId())
                .version(entity.getVersion())
                .procesadoEn(entity.getProcesadoEn())
                .build();
    }
}
