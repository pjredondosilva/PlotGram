package es.plotgram.backend.servicios;

import es.plotgram.backend.rest.dto.DNoticia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ServicioNoticiasTest {

    @Test
    @DisplayName("getNoticias OK: recupera noticias de la cache correctamente")
    void testGetNoticiasCache() {
        ServicioNoticias servicio = new ServicioNoticias();
        List<DNoticia> cache = new CopyOnWriteArrayList<>();
        cache.add(new DNoticia("id-123", "Titulo Cache", "Desc", "url", "img", "Src", "es", Instant.now()));
        ReflectionTestUtils.setField(servicio, "cacheNoticias", cache);
        var noticias = servicio.getNoticias();
        assertThat(noticias).hasSize(1);
        assertThat(noticias.get(0).titulo()).isEqualTo("Titulo Cache");
    }

    @Test
    @DisplayName("limpiarHtml OK: elimina etiquetas de forma robusta")
    void testLimpiarHtml() {
        ServicioNoticias servicio = new ServicioNoticias();
        String htmlIn = "<div>Texto <b>con</b> etiquetas <br/></div>";
        String resultado = (String) ReflectionTestUtils.invokeMethod(servicio, "limpiarHtml", htmlIn);
        assertThat(resultado).isEqualTo("Texto con etiquetas");
    }

    @Test
    @DisplayName("extraerImagen OK: obtiene la URL desde el enclosure si existe")
    void testExtraerImagenConEnclosure() {
        ServicioNoticias servicio = new ServicioNoticias();

        com.rometools.rome.feed.synd.SyndEntry entry = org.mockito.Mockito.mock(com.rometools.rome.feed.synd.SyndEntry.class);
        com.rometools.rome.feed.synd.SyndEnclosure enclosure = org.mockito.Mockito.mock(com.rometools.rome.feed.synd.SyndEnclosure.class);

        org.mockito.Mockito.when(enclosure.getUrl()).thenReturn("https://test.com/imagen.jpg");
        org.mockito.Mockito.when(entry.getEnclosures()).thenReturn(List.of(enclosure));

        String resultado = (String) ReflectionTestUtils.invokeMethod(servicio, "extraerImagen", entry);
        assertThat(resultado).isEqualTo("https://test.com/imagen.jpg");
    }

    @Test
    @DisplayName("extraerImagen OK: obtiene la URL mediante Regex en la descripcion si no hay enclosure")
    void testExtraerImagenConRegex() {
        ServicioNoticias servicio = new ServicioNoticias();

        com.rometools.rome.feed.synd.SyndEntry entry = org.mockito.Mockito.mock(com.rometools.rome.feed.synd.SyndEntry.class);
        com.rometools.rome.feed.synd.SyndContent desc = org.mockito.Mockito.mock(com.rometools.rome.feed.synd.SyndContent.class);

        org.mockito.Mockito.when(entry.getEnclosures()).thenReturn(List.of());
        org.mockito.Mockito.when(desc.getValue()).thenReturn("<p>Un texto <img src=\"https://test.com/regex.jpg\" alt=\"x\"/></p>");
        org.mockito.Mockito.when(entry.getDescription()).thenReturn(desc);

        String resultado = (String) ReflectionTestUtils.invokeMethod(servicio, "extraerImagen", entry);
        assertThat(resultado).isEqualTo("https://test.com/regex.jpg");
    }

    @Test
    @DisplayName("extraerImagen OK: devuelve null si no encuentra ninguna imagen")
    void testExtraerImagenSinImagen() {
        ServicioNoticias servicio = new ServicioNoticias();

        com.rometools.rome.feed.synd.SyndEntry entry = org.mockito.Mockito.mock(com.rometools.rome.feed.synd.SyndEntry.class);
        com.rometools.rome.feed.synd.SyndContent desc = org.mockito.Mockito.mock(com.rometools.rome.feed.synd.SyndContent.class);

        org.mockito.Mockito.when(entry.getEnclosures()).thenReturn(List.of());
        org.mockito.Mockito.when(desc.getValue()).thenReturn("<p>Un texto sin imagen</p>");
        org.mockito.Mockito.when(entry.getDescription()).thenReturn(desc);

        String resultado = (String) ReflectionTestUtils.invokeMethod(servicio, "extraerImagen", entry);
        assertThat(resultado).isNull();
    }
}
