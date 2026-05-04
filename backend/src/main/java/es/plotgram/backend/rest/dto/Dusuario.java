package es.plotgram.backend.rest.dto;

import es.plotgram.backend.entidades.Tipousuario;

/**
 * DTO para la representación de un usuario.
 * Incluye contadores de seguidores y seguidos para la integración social.
 */
public record Dusuario(
        Long id,
        String nombre,
        String contrasenia, // Normalmente nulo en respuestas
        String email,
        String fotoPerfil,
        String descripcion,
        Tipousuario tipo,
        boolean borrado,
        long seguidoresCount,
        long seguidosCount,
        Boolean loSigo // Nulo si no hay contexto de autenticación
) {
}
