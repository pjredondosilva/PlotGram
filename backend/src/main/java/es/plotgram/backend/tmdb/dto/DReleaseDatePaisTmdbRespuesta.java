package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DReleaseDatePaisTmdbRespuesta(
        @JsonProperty("iso_3166_1")
        String iso31661,
        List<DReleaseDateItemTmdbRespuesta> releaseDates
) {
}