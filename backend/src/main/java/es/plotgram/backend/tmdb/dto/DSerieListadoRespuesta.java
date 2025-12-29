package es.plotgram.backend.tmdb.dto;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DSerieListadoRespuesta(
        long id,
        String name,
        String firstAirDate,
        String posterPath
) {
}
