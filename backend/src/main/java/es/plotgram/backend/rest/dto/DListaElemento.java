package es.plotgram.backend.rest.dto;

import es.plotgram.backend.entidades.TipoContenido;

import java.time.LocalDate;

public record DListaElemento(
        Long idItem,
        Integer orden,
        Long idContenido,
        Long tmdbId,
        TipoContenido tipo,
        String titulo,
        String imagen,
        LocalDate fechaPublicacion,
        String sinopsis,
        String enlace,
        Long serieTmdbId,
        Integer numeroTemporada,
        Integer numeroEpisodio
) {
}