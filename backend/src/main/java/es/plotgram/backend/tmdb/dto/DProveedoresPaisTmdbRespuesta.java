package es.plotgram.backend.tmdb.dto;

import java.util.List;

public record DProveedoresPaisTmdbRespuesta(
        String link,
        List<DProveedorTmdbRespuesta> flatrate,
        List<DProveedorTmdbRespuesta> rent,
        List<DProveedorTmdbRespuesta> buy
) {
}