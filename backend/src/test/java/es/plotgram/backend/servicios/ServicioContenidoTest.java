package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Pelicula;
import es.plotgram.backend.entidades.Serie;
import es.plotgram.backend.entidades.TipoContenido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = es.plotgram.backend.app.BackendApplication.class,
        properties = {
                "app.auth.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "app.auth.jwt.clockSkewSeconds=60",
                "tmdb.base-url=http://localhost:1",
                "tmdb.token=test-token",
                "plotgram.admin.nombre=admin-test",
                "plotgram.admin.password=Admin123!",
                "plotgram.admin.email=admin-test@plotgram.test",
                "gemini.api-key=test-key"
        }
)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ServicioContenidoTest {

    @Autowired
    private ServicioContenido servicioContenido;

    @Test
    @DisplayName("buscarOGuardar OK: guarda el contenido si no existe por tmdbId y tipo")
    void testBuscarOGuardarContenidoNuevo() {
        Pelicula pelicula = pelicula(101L, "Matrix ServicioContenido");

        var resultado = servicioContenido.buscarOGuardar(pelicula);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getTmdbId()).isEqualTo(101L);
        assertThat(resultado.getTipo()).isEqualTo(TipoContenido.PELICULA);
        assertThat(resultado.getTitulo()).isEqualTo("Matrix ServicioContenido");
    }

    @Test
    @DisplayName("buscarOGuardar OK: devuelve el contenido existente si coincide tmdbId y tipo")
    void testBuscarOGuardarContenidoExistente() {
        Pelicula peliculaOriginal = pelicula(102L, "Origen Original");
        var guardada = servicioContenido.buscarOGuardar(peliculaOriginal);

        Pelicula peliculaRepetida = pelicula(102L, "Origen Repetida");
        var resultado = servicioContenido.buscarOGuardar(peliculaRepetida);

        assertThat(resultado.getId()).isEqualTo(guardada.getId());
        assertThat(resultado.getTitulo()).isEqualTo("Origen Original");
    }

    @Test
    @DisplayName("buscarOGuardar OK: permite mismo tmdbId si el tipo de contenido es distinto")
    void testBuscarOGuardarMismoTmdbIdDistintoTipo() {
        Pelicula pelicula = pelicula(103L, "Fargo Pelicula");
        Serie serie = serie(103L, "Fargo Serie");

        var peliculaGuardada = servicioContenido.buscarOGuardar(pelicula);
        var serieGuardada = servicioContenido.buscarOGuardar(serie);

        assertThat(peliculaGuardada.getId()).isNotEqualTo(serieGuardada.getId());
        assertThat(peliculaGuardada.getTipo()).isEqualTo(TipoContenido.PELICULA);
        assertThat(serieGuardada.getTipo()).isEqualTo(TipoContenido.SERIE);
    }

    private Pelicula pelicula(Long tmdbId, String titulo) {
        Pelicula pelicula = new Pelicula();
        pelicula.setTmdbId(tmdbId);
        pelicula.setTitulo(titulo);
        pelicula.setImagen("/" + tmdbId + ".jpg");
        pelicula.setFechaPublicacion(LocalDate.of(2020, 1, 1));
        pelicula.setSinopsis("Sinopsis " + titulo);
        pelicula.setEnlace("https://plotgram.test/peliculas/" + tmdbId);
        return pelicula;
    }

    private Serie serie(Long tmdbId, String titulo) {
        Serie serie = new Serie();
        serie.setTmdbId(tmdbId);
        serie.setTitulo(titulo);
        serie.setImagen("/serie-" + tmdbId + ".jpg");
        serie.setFechaPublicacion(LocalDate.of(2021, 2, 2));
        serie.setSinopsis("Sinopsis " + titulo);
        serie.setEnlace("https://plotgram.test/series/" + tmdbId);
        return serie;
    }
}
