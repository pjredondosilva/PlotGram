package es.plotgram.backend.rest.dto;

/**
 * Representa un mensaje individual en la conversación del chat.
 * @param rol El rol del emisor (user o assistant).
 * @param contenido El contenido del mensaje.
 */
public record DMensajeChat(String rol, String contenido) {}
