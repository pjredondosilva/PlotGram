package es.plotgram.backend.rest.dto;

import java.util.List;

public record DListaDetalle(
        Long id,
        String nombre,
        String descripcion,
        String imagenPortada,
        List<DListaElemento> elementos
) {
}