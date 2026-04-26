package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DSerieDetalleRespuesta(
        long id,
        String name,
        String overview,

        @JsonProperty("first_air_date")
        String firstAirDate,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("backdrop_path")
        String backdropPath,

        @JsonProperty("number_of_seasons")
        Integer numberOfSeasons,

        @JsonProperty("number_of_episodes")
        Integer numberOfEpisodes,

        @JsonProperty("vote_average")
        Double voteAverage,

        @JsonProperty("origin_country")
        List<String> originCountry,

        @JsonProperty("created_by")
        List<DCreadorSerieTmdbRespuesta> createdBy,
        String status,
        List<DGeneroTmdb> genres,
        List<DTemporadaResumenRespuesta> seasons,
        DCreditosTmdbRespuesta credits,
        DVideosTmdbRespuesta videos,
        @JsonProperty("watch/providers")
        DProveedoresConsultaTmdbRespuesta watchProviders,
        DSeriesRelacionadasTmdbRespuesta recommendations
) {
}