package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ProcesoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResultadoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.persistence.entity.IntentoSincronizacionEntity;

@Component
public class IntentoSincronizacionMapper {

    public IntentoSincronizacionEntity toEntity(IntentoSincronizacion intento) {
        if (intento == null) {
            return null;
        }
        return IntentoSincronizacionEntity.builder()
                .id(intento.getId())
                .proceso(intento.getProceso().name())
                .inicio(intento.getInicio())
                .fin(intento.getFin())
                .resultado(intento.getResultado() == null ? null : intento.getResultado().name())
                .intentos(intento.getIntentos())
                .versionAplicada(intento.getVersionAplicada())
                .mensaje(intento.getMensaje())
                .build();
    }

    public IntentoSincronizacion toDomainModel(IntentoSincronizacionEntity entity) {
        if (entity == null) {
            return null;
        }
        return IntentoSincronizacion.builder()
                .id(entity.getId())
                .proceso(ProcesoSincronizacion.valueOf(entity.getProceso()))
                .inicio(entity.getInicio())
                .fin(entity.getFin())
                .resultado(entity.getResultado() == null ? null : ResultadoSincronizacion.valueOf(entity.getResultado()))
                .intentos(entity.getIntentos())
                .versionAplicada(entity.getVersionAplicada())
                .mensaje(entity.getMensaje())
                .build();
    }
}
