package es.plotgram.backend.rest.dto;

import es.plotgram.backend.entidades.Tipousuario;

public record Dusuario(
        Long id,
        String nombre,
        String contrasenia,
        String email,
        String fotoPerfil,
        String descripcion,
        Tipousuario tipo,
        boolean borrado
) {
}
