package es.plotgram.backend.rest.dto.tmdb;

import java.util.List;

public record DEpisodioDetalle(
        long id,
        String name,
        String overview,
        Integer episodeNumber,
        Integer seasonNumber,
        String airDate,
        String stillPath,
        Integer runtime,
        Double voteAverage,
        List<DActorTmdb> cast
) {
}
