package es.plotgram.backend.rest.dto.valoraciones;

import java.time.LocalDateTime;

public record DValoracionResumen(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        String usuarioFoto,
        int puntuacion,
        String comentario,
        LocalDateTime fecha
) {}
