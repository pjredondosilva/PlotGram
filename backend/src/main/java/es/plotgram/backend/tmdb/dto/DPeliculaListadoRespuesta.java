package es.plotgram.backend.tmdb.dto;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DPeliculaListadoRespuesta(
        long id,
        String title,
        String releaseDate,
        String posterPath,
        List<Integer> genreIds
) {
}
