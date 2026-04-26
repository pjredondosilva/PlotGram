package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DPeliculasRelacionadasTmdbRespuesta(
        Integer page,
        List<DPeliculaListadoRespuesta> results,
        @JsonProperty("total_pages")
        Integer totalPages,
        @JsonProperty("total_results")
        Integer totalResults
) {
}