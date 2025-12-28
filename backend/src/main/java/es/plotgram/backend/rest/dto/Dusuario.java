package es.plotgram.backend.rest.dto;

import es.plotgram.backend.entidades.tipousuario;

public record Dusuario(
        long id,
        String nombre,
        String contrasenia,
        String email,
        tipousuario tipo,
        boolean borrado
) {
}
