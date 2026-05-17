package es.plotgram.backend.servicios;

import es.plotgram.backend.rest.dto.DMensajeChat;
import es.plotgram.backend.rest.dto.DPeticionChat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioChatTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ServicioChat servicioChat;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(servicioChat, "apiKey", "clave-secreta-chat");
        ReflectionTestUtils.setField(servicioChat, "modelo", "modelo-ia-test");
    }

    @Test
    @DisplayName("procesarChat OK: el servicio formatea correctamente la respuesta de Gemini")
    void testProcesarChatExito() throws Exception {
        DPeticionChat request = new DPeticionChat(
                List.of(new DMensajeChat("user", "Hola PlotBot")),
                "Contexto Unico de Prueba"
        );

        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(Map.of("text", "Respuesta de IA Unica"))
                        ))
                )
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        String respuesta = servicioChat.procesarChat(request);
        assertThat(respuesta).isEqualTo("Respuesta de IA Unica");
    }

    @Test
    @DisplayName("procesarChat KO: lanza excepcion si la API de Gemini falla")
    void testProcesarChatError() {
        DPeticionChat request = new DPeticionChat(List.of(), null);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_GATEWAY));
        assertThrows(RuntimeException.class, () -> servicioChat.procesarChat(request));
    }
}
