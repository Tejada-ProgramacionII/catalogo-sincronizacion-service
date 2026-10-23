package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.VersionAplicadaEntity;

@Component
public class VersionAplicadaMapper {

    public VersionAplicadaEntity toEntity(VersionAplicada versionAplicada) {
        if (versionAplicada == null) {
            return null;
        }
        return VersionAplicadaEntity.builder()
                .id(VersionAplicadaEntity.ID_UNICO)
                .version(versionAplicada.getVersion())
                .aplicadaEn(versionAplicada.getAplicadaEn())
                .build();
    }

    public VersionAplicada toDomainModel(VersionAplicadaEntity entity) {
        if (entity == null) {
            return null;
        }
        return VersionAplicada.builder()
                .version(entity.getVersion())
                .aplicadaEn(entity.getAplicadaEn())
                .build();
    }
}
