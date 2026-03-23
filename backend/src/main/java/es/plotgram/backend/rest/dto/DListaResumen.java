package es.plotgram.backend.rest.dto;

public record DListaResumen(
        Long id,
        String nombre,
        String descripcion,
        String imagenPortada,
        long numeroElementos
) {
}