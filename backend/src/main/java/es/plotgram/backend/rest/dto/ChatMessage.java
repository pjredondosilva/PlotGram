package es.plotgram.backend.rest.dto;

/**
 * Representa un mensaje individual en la conversación del chat.
 * @param role El rol del emisor (user o assistant).
 * @param content El contenido del mensaje.
 */
public record ChatMessage(String role, String content) {}
