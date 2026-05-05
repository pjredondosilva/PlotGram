package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.ChatRequest;
import es.plotgram.backend.servicios.ServicioChat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST que expone el punto de entrada para el Asistente Inteligente (Chatbot).
 * Actúa como puente entre el frontend y el Servicio de Chat, gestionando las peticiones HTTP.
 * 
 * Sigue los principios de responsabilidad única al delegar la lógica de IA al servicio.
 */
@RestController
@RequestMapping("/api/chat")
public class ControladorChat {

    private final ServicioChat servicioChat;

    /**
     * Constructor del controlador.
     * @param servicioChat Servicio de IA inyectado por Spring.
     */
    public ControladorChat(ServicioChat servicioChat) {
        this.servicioChat = servicioChat;
    }

    /**
     * Punto de entrada POST para interactuar con el chatbot.
     * 
     * @param request Cuerpo de la petición con mensajes y contexto.
     * @return Respuesta con el contenido generado por la IA o mensaje de error.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request) {
        try {
            String respuesta = servicioChat.procesarChat(request);
            return ResponseEntity.ok(Map.of("content", respuesta.trim()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Fallo al conectar con el asistente: " + e.getMessage()));
        }
    }
}
