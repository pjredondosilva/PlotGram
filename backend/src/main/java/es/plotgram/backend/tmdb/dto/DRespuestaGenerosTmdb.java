package es.plotgram.backend.tmdb.dto;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DRespuestaGenerosTmdb(
        List<DGeneroTmdb> genres
) {
}
