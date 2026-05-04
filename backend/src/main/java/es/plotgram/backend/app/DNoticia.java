package es.plotgram.backend.app;

import java.time.Instant;

/**
 * Data Transfer Object (DTO) que representa una noticia de cine.
 * Utiliza un Record de Java para garantizar la inmutabilidad y concisión.
 *
 * @param id Identificador único (UUID).
 * @param titulo Titular de la noticia.
 * @param descripcion Resumen o contenido breve de la noticia.
 * @param url Enlace directo a la fuente original.
 * @param imagen URL de la imagen de portada.
 * @param fuente Nombre del medio de comunicación (Espinof, Variety, etc.).
 * @param idioma Código de idioma (es, us).
 * @param fechaPublicacion Instante de publicación de la noticia.
 */
public record DNoticia(
        String id,
        String titulo,
        String descripcion,
        String url,
        String imagen,
        String fuente,
        String idioma,
        Instant fechaPublicacion
) {}
