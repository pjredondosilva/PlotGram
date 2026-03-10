package es.plotgram.backend.rest.dto.tmdb;

import java.util.List;

public record DPeliculaListado(
        long id,
        String title,
        String releaseDate,
        String posterPath,
        List<Integer> genreIds,
        List<String> genres
){}
