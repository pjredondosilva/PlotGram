package es.plotgram.backend.rest.dto.tmdb;

import java.util.List;

public record DSerieRelacionada(
        long id,
        String name,
        String firstAirDate,
        String posterPath
) {
}