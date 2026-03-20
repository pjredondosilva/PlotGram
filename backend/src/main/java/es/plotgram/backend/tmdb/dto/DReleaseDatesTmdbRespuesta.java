package es.plotgram.backend.tmdb.dto;

import java.util.List;

public record DReleaseDatesTmdbRespuesta(
        List<DReleaseDatePaisTmdbRespuesta> results
) {
}