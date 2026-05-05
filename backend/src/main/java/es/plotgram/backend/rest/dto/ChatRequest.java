package es.plotgram.backend.rest.dto;

import java.util.List;

/**
 * Representa una solicitud de chat enviada desde el frontend.
 * Contiene el historial de mensajes y el contexto opcional de la interfaz.
 */
public record ChatRequest(
    List<ChatMessage> messages,
    String uiContext
) {}
