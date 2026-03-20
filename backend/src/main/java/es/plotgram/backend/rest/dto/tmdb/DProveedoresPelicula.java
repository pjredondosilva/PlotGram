package es.plotgram.backend.rest.dto.tmdb;

import java.util.List;

public record DProveedoresPelicula(
        List<DProveedor> stream,
        List<DProveedor> rent,
        List<DProveedor> buy
) {
}