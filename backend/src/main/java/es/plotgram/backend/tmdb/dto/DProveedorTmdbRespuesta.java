package es.plotgram.backend.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DProveedorTmdbRespuesta(
        @JsonProperty("provider_id")
        Integer providerId,

        @JsonProperty("provider_name")
        String providerName,

        @JsonProperty("logo_path")
        String logoPath
) {
}