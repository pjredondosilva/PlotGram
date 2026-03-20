package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DReleaseDateItemTmdbRespuesta(
        String certification,
        String note,

        @JsonProperty("release_date")
        String releaseDate,

        Integer type
) {
}