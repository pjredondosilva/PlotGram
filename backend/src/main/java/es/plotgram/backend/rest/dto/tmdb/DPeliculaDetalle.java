package es.plotgram.backend.rest.dto.tmdb;

import java.util.List;

public record DPeliculaDetalle(
        long id,
        String title,
        String overview,
        String releaseDate,
        String posterPath,
        String backdropPath,
        Integer runtime,
        Double voteAverage,
        List<String> genres,
        List<DActorTmdb> cast,
        DTrailer trailer,
        DProveedoresPelicula providers,
        List<DPeliculaRecomendada> recommendations,
        String director,
        String guionista,
        Long budget,
        Long revenue,
        String paisOrigen,
        Boolean estrenadaEnCines
) {
}