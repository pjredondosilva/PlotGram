package es.plotgram.backend.rest.dto.tmdb;

public record DPeliculaRelacionada(
        long id,
        String title,
        String releaseDate,
        String posterPath
) {
}