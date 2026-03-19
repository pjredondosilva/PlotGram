package es.plotgram.backend.rest.dto.tmdb;

import java.util.List;

public record DRespuestaPaginadaTmdb<T>(
        int page,
        int totalPages,
        int totalResults,
        List<T> results
) {
}