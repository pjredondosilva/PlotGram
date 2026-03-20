package es.plotgram.backend.rest.dto.tmdb;

public record DActorTmdb(
        long id,
        String name,
        String character,
        String profilePath
) {
}
