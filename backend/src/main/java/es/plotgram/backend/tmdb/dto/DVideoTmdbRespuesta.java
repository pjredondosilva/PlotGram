package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DVideoTmdbRespuesta(
        String name,
        String key,
        String site,
        String type,
        Boolean official
) {
}