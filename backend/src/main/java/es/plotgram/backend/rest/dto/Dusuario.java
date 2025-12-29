package es.plotgram.backend.rest.dto;

import es.plotgram.backend.entidades.Tipousuario;

public record Dusuario(
        long id,
        String nombre,
        String contrasenia,
        String email,
        Tipousuario tipo,
        boolean borrado
) {
}
