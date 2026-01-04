package es.plotgram.backend.tmdb.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DSerieListadoRespuesta(
        long id,
        String name,
        @JsonProperty("first_air_date")
        String firstAirDate,
        @JsonProperty("poster_path")
        String posterPath,
        @JsonProperty("genre_ids")
        List<Integer> genreIds
) {
}
