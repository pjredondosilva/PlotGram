package es.plotgram.backend.rest.dto.tmdb;

public record DEpisodioListado(
        long id,
        String name,
        String overview,
        Integer episodeNumber,
        Integer seasonNumber,
        String airDate,
        String stillPath,
        Double voteAverage
) {
}
