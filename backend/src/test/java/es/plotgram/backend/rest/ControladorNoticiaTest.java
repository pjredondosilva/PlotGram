package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.DNoticia;
import es.plotgram.backend.servicios.ServicioNoticias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControladorNoticiaTest {

    @Mock
    private ServicioNoticias servicioNoticias;

    @InjectMocks
    private ControladorNoticia controladorNoticia;

    @Test
    @DisplayName("GET /api/noticias OK: filtra noticias por idioma y paginación")
    void testObtenerNoticias() {
        // GIVEN
        DNoticia n1 = new DNoticia("id-n1", "Noticia ES", "Desc", "url", "img", "Fuente", "es", Instant.now());
        DNoticia n2 = new DNoticia("id-n2", "Noticia US", "Desc", "url", "img", "Fuente", "us", Instant.now());
        
        when(servicioNoticias.getNoticias()).thenReturn(List.of(n1, n2));

        // WHEN
        var resultadoES = controladorNoticia.obtenerNoticias("es", 1, 10);
        var resultadoUS = controladorNoticia.obtenerNoticias("us", 1, 10);

        // THEN
        assertThat(resultadoES).hasSize(1);
        assertThat(resultadoES.get(0).titulo()).isEqualTo("Noticia ES");
        
        assertThat(resultadoUS).hasSize(1);
        assertThat(resultadoUS.get(0).titulo()).isEqualTo("Noticia US");
    }

    @Test
    @DisplayName("GET /api/noticias OK: maneja paginación vacía")
    void testObtenerNoticiasVacias() {
        // GIVEN
        when(servicioNoticias.getNoticias()).thenReturn(List.of());

        // WHEN
        var resultado = controladorNoticia.obtenerNoticias("es", 1, 10);

        // THEN
        assertThat(resultado).isEmpty();
    }
}
