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
}
