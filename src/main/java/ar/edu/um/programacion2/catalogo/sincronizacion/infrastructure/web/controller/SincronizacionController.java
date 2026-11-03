package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.um.programacion2.catalogo.sincronizacion.application.service.SincronizacionService;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.dto.EstadoSincronizacionResponse;
import ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.web.mapper.SincronizacionDtoMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sincronizacion")
@RequiredArgsConstructor
public class SincronizacionController {

    private final SincronizacionService sincronizacionService;
    private final SincronizacionDtoMapper sincronizacionDtoMapper;

    @GetMapping("/estado")
    public ResponseEntity<EstadoSincronizacionResponse> consultarEstado() {
        return ResponseEntity.ok(sincronizacionDtoMapper.toEstadoResponse(sincronizacionService.consultarEstado()));
    }
}
