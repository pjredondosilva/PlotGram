package es.plotgram.backend.tmdb.dto;

import java.util.List;

public record DRecomendacionesSeriesTmdbRespuesta(
        List<DSerieListadoRespuesta> results
) {
}