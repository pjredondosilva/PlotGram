package es.plotgram.backend.rest.dto.tmdb;

import java.util.List;

public record DSerieDetalle(
        long id,
        String name,
        String overview,
        String firstAirDate,
        String posterPath,
        String backdropPath,
        Integer numberOfSeasons,
        Integer numberOfEpisodes,
        Double voteAverage,
        List<String> genres,
        List<DTemporadaResumen> seasons,
        List<DActorTmdb> cast,
        String creator,
        String paisOrigen,
        String status,
        DProveedoresPelicula providers,
        List<DSerieRelacionada> recommendations
) {
}