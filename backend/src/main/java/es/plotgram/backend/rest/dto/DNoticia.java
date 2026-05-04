package es.plotgram.backend.rest.dto;

import java.time.Instant;

/**
 * DTO para representar una noticia de cine.
 */
public record DNoticia(
    String id,
    String titulo,
    String descripcion,
    String url,
    String imagen,
    String fuente,
    String idioma, // "es" o "us"
    Instant fechaPublicacion
) {}
