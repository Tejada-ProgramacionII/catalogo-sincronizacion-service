package ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.um.programacion2.catalogo.profesional.application.exception.FiltroBusquedaInvalidoException;
import ar.edu.um.programacion2.catalogo.profesional.application.service.ProfesionalService;
import ar.edu.um.programacion2.catalogo.profesional.domain.model.FiltroBusquedaProfesionales;
import ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.mapper.ProfesionalDtoMapper;
import ar.edu.um.programacion2.catalogo.sincronizacion.application.exception.CatalogoNoDisponibleException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profesionales")
@RequiredArgsConstructor
public class ProfesionalController {

    private final ProfesionalService profesionalService;
    private final ProfesionalDtoMapper profesionalDtoMapper;

    @GetMapping
    public ResponseEntity<Object> buscarProfesionales(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean habilitado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        FiltroBusquedaProfesionales filtro = profesionalDtoMapper.toFiltro(categoriaId, nombre, habilitado, fecha);
        try {
            return ResponseEntity.ok(profesionalDtoMapper.toPaginaResponse(
                    profesionalService.buscarProfesionales(filtro, page, size)));
        } catch (FiltroBusquedaInvalidoException e) {
            return ResponseEntity.badRequest()
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (CatalogoNoDisponibleException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage()));
        }
    }
}
