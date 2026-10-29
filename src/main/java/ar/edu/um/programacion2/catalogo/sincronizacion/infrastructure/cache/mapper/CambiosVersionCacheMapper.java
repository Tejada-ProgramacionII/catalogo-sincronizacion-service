package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.mapper;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.CambiosVersion;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.dto.CambiosVersionCatedraResponse;

@Component
public class CambiosVersionCacheMapper {

    public CambiosVersion toDomainModel(CambiosVersionCatedraResponse response) {
        if (response == null) {
            return null;
        }
        return CambiosVersion.builder()
                .version(response.getVersion())
                .categoriaIds(response.getChanges().getProfessionalCategories())
                .profesionalIds(response.getChanges().getProfessionals())
                .horarioIds(response.getChanges().getWeeklySchedules())
                .build();
    }
}
