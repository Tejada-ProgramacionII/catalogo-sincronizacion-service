package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.cache.dto;

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
public class CambiosCatedraResponse {
    private List<Long> professionalCategories;
    private List<Long> professionals;
    private List<Long> weeklySchedules;
}
