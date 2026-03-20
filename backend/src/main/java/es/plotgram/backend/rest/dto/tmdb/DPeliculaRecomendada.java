package es.plotgram.backend.rest.dto.tmdb;

public record DPeliculaRecomendada(
        long id,
        String title,
        String releaseDate,
        String posterPath
) {
}