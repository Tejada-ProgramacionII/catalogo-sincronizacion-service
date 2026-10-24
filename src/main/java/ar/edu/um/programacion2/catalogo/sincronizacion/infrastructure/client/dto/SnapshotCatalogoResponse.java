package ar.edu.um.programacion2.catalogo.sincronizacion.infrastructure.client.dto;

import java.time.Instant;
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
public class SnapshotCatalogoResponse {
    private Long snapshotVersion;
    private Instant generatedAt;
    private List<CategoriaCatedraResponse> professionalCategories;
    private List<ProfesionalCatedraResponse> professionals;
    private List<HorarioSemanalCatedraResponse> weeklySchedules;
}
