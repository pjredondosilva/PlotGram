package es.plotgram.backend.rest.dto.tmdb;

public record DTemporadaResumen(
        long id,
        String name,
        String overview,
        Integer seasonNumber,
        Integer episodeCount,
        String airDate,
        String posterPath
) {
}
