package es.plotgram.backend.servicios;

import es.plotgram.backend.rest.dto.DMensajeChat;
import es.plotgram.backend.rest.dto.DPeticionChat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Servicio encargado de la comunicación con la API de Google Gemini.
 * Gestiona el envío de mensajes, el contexto de la interfaz y la búsqueda web.
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
        Si tienes la información en el contexto, úsala con seguridad.
        """;

    public ServicioChat(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Procesa una solicitud de chat enviándola a la API de Gemini.
     */
    public String procesarChat(DPeticionChat request) throws Exception {
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("${")) {
            throw new IllegalStateException("La API Key de Gemini no está configurada correctamente.");
        }

        String finalUrl = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent", modelo);

        List<Map<String, Object>> contents = new ArrayList<>();

        // 1. Añadimos el prompt de sistema y el contexto como un mensaje de 'user' al principio
        // (Gemini v1beta a veces prefiere el sistema en el historial si no se usa el campo 'system_instruction')
        String textoInicial = promptsistema;
        if (request.contextoUI() != null && !request.contextoUI().isEmpty()) {
            textoInicial += "\n\nContexto UI actual: " + request.contextoUI();
        }

        contents.add(Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", textoInicial))
        ));

        // 2. Añadimos el historial
        for (DMensajeChat msg : request.mensajes()) {
            contents.add(Map.of(
                "role", msg.rol(),
                "parts", List.of(Map.of("text", msg.contenido()))
            ));
        }

        Map<String, Object> body = Map.of(
            "contents", contents,
            "tools", List.of(Map.of("google_search", Map.of()))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(finalUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List candidates = (List) response.getBody().get("candidates");
                Map firstCandidate = (Map) candidates.get(0);
                Map content = (Map) firstCandidate.get("content");
                List parts = (List) content.get("parts");
                Map firstPart = (Map) parts.get(0);
                return (String) firstPart.get("text");
            } else {
                throw new RuntimeException("Error en la respuesta de Gemini: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Fallo al conectar con el asistente: " + e.getMessage());
        }
    }
}
