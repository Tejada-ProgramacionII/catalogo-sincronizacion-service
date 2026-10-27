package ar.edu.um.programacion2.catalogo.profesional.infrastructure.web.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginaResponse<T> {
    private List<T> contenido;
    private int numero;
    private int tamanio;
    private long totalElementos;
    private int totalPaginas;
}
