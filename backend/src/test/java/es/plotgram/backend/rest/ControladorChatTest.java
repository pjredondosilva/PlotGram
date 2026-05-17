package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.DPeticionChat;
import es.plotgram.backend.rest.dto.DMensajeChat;
import es.plotgram.backend.servicios.ServicioChat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControladorChatTest {

    @Mock
    private ServicioChat servicioChat;

    @InjectMocks
    private ControladorChat controladorChat;

    @Test
    @DisplayName("POST /api/chat OK: el controlador delega correctamente en el servicio")
    void testChatExito() throws Exception {
        // GIVEN
        DPeticionChat request = new DPeticionChat(
                List.of(new DMensajeChat("user", "Pregunta de prueba")),
                "Contexto de pelicula"
        );
        when(servicioChat.procesarChat(any(DPeticionChat.class))).thenReturn("Respuesta Simulada");

        // WHEN
        var respuesta = controladorChat.chat(request);

        // THEN
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().get("content")).isEqualTo("Respuesta Simulada");
    }

    @Test
    @DisplayName("POST /api/chat KO: el controlador maneja excepciones del servicio")
    void testChatError() throws Exception {
        // GIVEN
        DPeticionChat request = new DPeticionChat(List.of(), null);
        when(servicioChat.procesarChat(any(DPeticionChat.class))).thenThrow(new RuntimeException("Fallo en la IA"));

        // WHEN
        var respuesta = controladorChat.chat(request);

        // THEN
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
