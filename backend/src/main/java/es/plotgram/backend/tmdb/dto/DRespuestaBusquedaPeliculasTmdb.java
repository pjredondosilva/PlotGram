package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DRespuestaBusquedaPeliculasTmdb(
        int page,
        List<DPeliculaListadoRespuesta> results,
        int totalPages,
        int totalResults
) {
}
