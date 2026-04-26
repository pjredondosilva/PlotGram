package es.plotgram.backend.tmdb.dto;

import java.util.List;

public record DSeriesRelacionadasTmdbRespuesta(
        List<DSerieListadoRespuesta> results
) {
}