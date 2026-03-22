package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DTemporadaDetalleRespuesta(
        long id,
        String name,
        String overview,
        @JsonProperty("season_number")
        Integer seasonNumber,
        @JsonProperty("episode_count")
        Integer episodeCount,
        @JsonProperty("air_date")
        String airDate,
        @JsonProperty("poster_path")
        String posterPath,
        DVideosTmdbRespuesta videos,
        List<DEpisodioListadoRespuesta> episodes
) {
}
