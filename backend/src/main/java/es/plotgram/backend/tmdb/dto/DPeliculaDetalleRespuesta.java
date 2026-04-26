package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DPeliculaDetalleRespuesta(
        long id,
        String title,
        String overview,

        @JsonProperty("release_date")
        String releaseDate,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("backdrop_path")
        String backdropPath,

        Integer runtime,
        Long budget,
        Long revenue,
        String status,

        @JsonProperty("production_countries")
        List<DPaisProduccionTmdbRespuesta> productionCountries,
        @JsonProperty("vote_average")
        Double voteAverage,

        List<DGeneroTmdb> genres,
        DCreditosTmdbRespuesta credits,
        DVideosTmdbRespuesta videos,
        DPeliculasRelacionadasTmdbRespuesta recommendations,

        @JsonProperty("watch/providers")
        DProveedoresConsultaTmdbRespuesta watchProviders,

        @JsonProperty("release_dates")
                DReleaseDatesTmdbRespuesta releaseDates

) {
}