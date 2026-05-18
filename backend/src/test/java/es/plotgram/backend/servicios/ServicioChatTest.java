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
    @org.mockito.Captor
    private org.mockito.ArgumentCaptor<HttpEntity<Map<String, Object>>> captor;

    @Test
    @DisplayName("procesarChat OK: inyecta herramientas, historial y contexto UI correctamente")
    void testProcesarChatEstructuraPeticion() throws Exception {
        DPeticionChat request = new DPeticionChat(
                List.of(new DMensajeChat("user", "Hola PlotBot")),
                "Pelicula: Matrix (1999)"
        );

        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(Map.of("content", Map.of("parts", List.of(Map.of("text", "Respuesta")))))
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        servicioChat.procesarChat(request);

        org.mockito.Mockito.verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
        HttpEntity<Map<String, Object>> entityEnviada = captor.getValue();

        Map<String, Object> body = entityEnviada.getBody();
        assertThat(body).isNotNull();

        List<?> tools = (List<?>) body.get("tools");
        assertThat(tools).isNotNull().isNotEmpty();
        @SuppressWarnings("unchecked")
        Map<String, Object> firstTool = (Map<String, Object>) tools.get(0);
        assertThat(firstTool).containsKey("google_search");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contents = (List<Map<String, Object>>) body.get("contents");

        String textoConContexto = null;
        for (Map<String, Object> contentMsg : contents) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMsg.get("parts");
            String texto = (String) parts.get(0).get("text");
            if (texto.contains("Contexto UI actual")) {
                textoConContexto = texto;
                break;
            }
        }

        assertThat(textoConContexto).isNotNull();
        assertThat(textoConContexto).contains("Contexto UI actual: Pelicula: Matrix (1999)");
    }
}