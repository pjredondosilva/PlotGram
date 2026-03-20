package es.plotgram.backend.tmdb.dto;

import java.util.List;

public record DCreditosTmdbRespuesta(
        List<DRepartoTmdbRespuesta> cast,
        List<DCrewTmdbRespuesta> crew
) {
}
