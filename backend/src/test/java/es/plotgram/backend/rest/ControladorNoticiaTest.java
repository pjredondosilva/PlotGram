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
    @DisplayName("GET /api/noticias OK: filtra noticias por idioma y paginacion")
    void testObtenerNoticias() {
        DNoticia n1 = new DNoticia("id-n1", "Noticia ES", "Desc", "url", "img", "Fuente", "es", Instant.now());
        DNoticia n2 = new DNoticia("id-n2", "Noticia US", "Desc", "url", "img", "Fuente", "us", Instant.now());

        when(servicioNoticias.getNoticias()).thenReturn(List.of(n1, n2));
        var resultadoES = controladorNoticia.obtenerNoticias("es", 1, 10);
        var resultadoUS = controladorNoticia.obtenerNoticias("us", 1, 10);
        assertThat(resultadoES).hasSize(1);
        assertThat(resultadoES.get(0).titulo()).isEqualTo("Noticia ES");

        assertThat(resultadoUS).hasSize(1);
        assertThat(resultadoUS.get(0).titulo()).isEqualTo("Noticia US");
    }

    @Test
    @DisplayName("GET /api/noticias OK: maneja paginacion vacia")
    void testObtenerNoticiasVacias() {
        when(servicioNoticias.getNoticias()).thenReturn(List.of());
        var resultado = controladorNoticia.obtenerNoticias("es", 1, 10);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("GET /api/noticias OK: paginacion real (25 noticias, pagina 2, tamano 20)")
    void testPaginacionReal() {
        List<DNoticia> mockNoticias = new java.util.ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            mockNoticias
                    .add(new DNoticia("id-" + i, "Noticia " + i, "Desc", "url", "img", "Fuente", "es", Instant.now()));
        }
        when(servicioNoticias.getNoticias()).thenReturn(mockNoticias);
        var resultado = controladorNoticia.obtenerNoticias(null, 2, 20);
        assertThat(resultado).hasSize(5);
        assertThat(resultado.get(0).titulo()).isEqualTo("Noticia 21");
        assertThat(resultado.get(4).titulo()).isEqualTo("Noticia 25");
    }

    @Test
    @DisplayName("GET /api/noticias OK: paginacion e idioma combinados (25 es, 5 us)")
    void testFiltroCombinado() {
        List<DNoticia> mockNoticias = new java.util.ArrayList<>();
        for (int i = 1; i <= 25; i++) mockNoticias.add(new DNoticia("es-" + i, "Noticia ES " + i, "Desc", "url", "img", "Fuente", "es", Instant.now()));
        for (int i = 1; i <= 5; i++) mockNoticias.add(new DNoticia("us-" + i, "Noticia US " + i, "Desc", "url", "img", "Fuente", "us", Instant.now()));

        when(servicioNoticias.getNoticias()).thenReturn(mockNoticias);
        var resultado = controladorNoticia.obtenerNoticias("es", 2, 20);

        assertThat(resultado).hasSize(5);
        assertThat(resultado.get(0).titulo()).isEqualTo("Noticia ES 21");
    }
}
