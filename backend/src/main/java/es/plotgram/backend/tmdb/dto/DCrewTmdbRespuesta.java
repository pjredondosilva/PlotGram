package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DCrewTmdbRespuesta(
        Long id,
        String name,
        String job,
        String department,

        @JsonProperty("profile_path")
        String profilePath
) {
}