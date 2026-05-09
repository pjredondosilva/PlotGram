package es.plotgram.backend.servicios;

import es.plotgram.backend.rest.dto.ChatMessage;
import es.plotgram.backend.rest.dto.ChatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Servicio encargado de la comunicación con la API de Inteligencia Artificial Google Gemini.
 * Centraliza la lógica de construcción de prompts, gestión de contexto y llamadas externas
 * para el chatbot de la plataforma.
 */
@Service
public class ServicioChat {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String modelo;

    private final RestTemplate restTemplate;

    private static final String promptsistema = """
        Eres un asistente experto en películas, series y entretenimiento dentro de la red social PlotGram.
        Responde solo sobre sinopsis, reparto, directores, estrenos, plataformas y curiosidades.
        Sé breve, en español y utiliza un formato ESTRUCTURADO y VISUAL:
        - Usa negritas para los títulos (ej: **Sinopsis:**).
        - Usa listas con guiones para los detalles.
        - Deja líneas en blanco entre secciones para mayor claridad.
        
        IMPORTANTE: Si se proporciona un 'Contexto UI', úsalo como tu fuente principal de verdad. 
        No menciones fechas de corte de conocimiento; si tienes la información en el contexto, úsala con seguridad.
        """;

    /**
     * Constructor del servicio.
     * @param restTemplate Cliente para realizar peticiones HTTP.
     */
    public ServicioChat(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Procesa una solicitud de chat enviándola a la API de Gemini y devolviendo la respuesta generada.
     * 
     * @param request Objeto que contiene los mensajes previos y el contexto opcional de la interfaz.
     * @return El contenido textual de la respuesta de la IA.
     * @throws Exception Si ocurre un error en la comunicación o la API devuelve un error de cuota/modelo.
     */
    public String procesarChat(ChatRequest request) throws Exception {
        if (apiKey == null || apiKey.equals("TU_CLAVE_AQUI") || apiKey.isEmpty()) {
            throw new IllegalStateException("La API Key de Gemini no está configurada en el servidor.");
        }

        // URL limpia sin la API Key expuesta
        String finalUrl = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent", modelo);
        
        // Construir el cuerpo para Gemini
        List<Map<String, Object>> contents = new ArrayList<>();
        
        // 1. Añadir Prompt de Sistema + Contexto
        String systemText = promptsistema;
        if (request.uiContext() != null && !request.uiContext().isEmpty()) {
            systemText += "\n\nContexto UI actual: " + request.uiContext();
        }
        
        contents.add(Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", systemText))
        ));
        
        // 2. Añadir historial de mensajes
        for (ChatMessage msg : request.messages()) {
            String geminiRole = msg.role().equals("assistant") ? "model" : "user";
            contents.add(Map.of(
                "role", geminiRole,
                "parts", List.of(Map.of("text", msg.content()))
            ));
        }

        Map<String, Object> body = Map.of("contents", contents);

        // CABECERAS: Autenticación robusta mediante cabecera
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(finalUrl, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List candidates = (List) response.getBody().get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");
            Map firstPart = (Map) parts.get(0);
            return (String) firstPart.get("text");
        } else {
            throw new RuntimeException("Error en la respuesta de la API de Gemini: " + response.getStatusCode());
        }
    }
}
