package es.plotgram.backend.rest.dto.tmdb;

import java.util.List;

public record DSerieListado(
        long id,
        String name,
        String firstAirDate,
        String posterPath,
        List<Integer>genreIds,
        List<String> genres
) {
}
