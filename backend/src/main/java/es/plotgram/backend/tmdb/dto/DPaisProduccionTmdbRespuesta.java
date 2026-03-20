package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DPaisProduccionTmdbRespuesta(
        @JsonProperty("iso_3166_1")
        String iso31661,
        String name
) {
}