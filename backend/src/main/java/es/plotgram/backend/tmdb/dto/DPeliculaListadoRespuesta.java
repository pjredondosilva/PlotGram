package es.plotgram.backend.tmdb.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DPeliculaListadoRespuesta(
        long id,
        String title,
        @JsonProperty("release_date")
        String releaseDate,
        @JsonProperty("poster_path")
        String posterPath,
        @JsonProperty("genre_ids")
        List<Integer> genreIds
) {
}
