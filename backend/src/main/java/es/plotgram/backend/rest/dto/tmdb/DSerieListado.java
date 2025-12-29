package es.plotgram.backend.rest.dto.tmdb;

public record DSerieListado(
        long id,
        String name,
        String firstAirDate,
        String posterPath
) {
}
