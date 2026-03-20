package es.plotgram.backend.rest.dto.tmdb;

import java.util.List;

public record DTemporadaDetalle(
        long id,
        String name,
        String overview,
        Integer seasonNumber,
        Integer episodeCount,
        String airDate,
        String posterPath,
        List<DEpisodioListado> episodes
) {
}
