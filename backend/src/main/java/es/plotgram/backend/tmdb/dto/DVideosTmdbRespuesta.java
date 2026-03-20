package es.plotgram.backend.tmdb.dto;

import java.util.List;

public record DVideosTmdbRespuesta(
        List<DVideoTmdbRespuesta> results
) {
}