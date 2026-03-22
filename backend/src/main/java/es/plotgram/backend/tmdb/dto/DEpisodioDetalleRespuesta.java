package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DEpisodioDetalleRespuesta(
        long id,
        String name,
        String overview,
        @JsonProperty("episode_number")
        Integer episodeNumber,
        @JsonProperty("season_number")
        Integer seasonNumber,
        @JsonProperty("air_date")
        String airDate,
        @JsonProperty("still_path")
        String stillPath,
        Integer runtime,
        @JsonProperty("vote_average")
        Double voteAverage,
        DCreditosTmdbRespuesta credits,
        @JsonProperty("guest_stars")
        List<DRepartoTmdbRespuesta> guestStars
) {
}