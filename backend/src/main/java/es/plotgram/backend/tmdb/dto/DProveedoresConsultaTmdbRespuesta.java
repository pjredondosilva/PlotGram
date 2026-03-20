package es.plotgram.backend.tmdb.dto;

import java.util.Map;

public record DProveedoresConsultaTmdbRespuesta(
        Map<String, DProveedoresPaisTmdbRespuesta> results
) {
}