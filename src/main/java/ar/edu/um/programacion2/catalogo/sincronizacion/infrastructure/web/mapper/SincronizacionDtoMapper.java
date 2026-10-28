package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.mapper;

import org.springframework.stereotype.Component;

import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.IntentoSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.ResumenSincronizacion;
import ar.edu.um.programacion2.catalogo.sincronizacion.domain.model.VersionAplicada;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.dto.EstadoSincronizacionResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.dto.IntentoSincronizacionResponse;

@Component
public class SincronizacionDtoMapper {

    public EstadoSincronizacionResponse toEstadoResponse(ResumenSincronizacion resumen) {
        if (resumen == null) {
            return null;
        }
        VersionAplicada version = resumen.getVersionAplicada();
        return EstadoSincronizacionResponse.builder()
                .estado(resumen.getEstado())
                .versionAplicada(version == null ? null : version.getVersion())
                .aplicadaEn(version == null ? null : version.getAplicadaEn())
                .ultimoIntento(toIntentoResponse(resumen.getUltimoIntento()))
                .build();
    }

    public IntentoSincronizacionResponse toIntentoResponse(IntentoSincronizacion intento) {
        if (intento == null) {
            return null;
        }
        return IntentoSincronizacionResponse.builder()
                .proceso(intento.getProceso())
                .inicio(intento.getInicio())
                .fin(intento.getFin())
                .resultado(intento.getResultado())
                .intentos(intento.getIntentos())
                .mensaje(intento.getMensaje())
                .build();
    }
}
