package es.plotgram.backend.rest;

import es.plotgram.backend.entidades.Pelicula;
import es.plotgram.backend.entidades.Serie;
import es.plotgram.backend.entidades.TipoContenido;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.rest.dto.DContenidoListaNuevo;
import es.plotgram.backend.rest.dto.DValoracionNueva;
import es.plotgram.backend.servicios.ServicioUsuario;
import es.plotgram.backend.servicios.ServicioValoracion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
                "gemini.api-key=test-key",
                "gemini.model=gemini-2.5-flash"
        }
)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ControladorValoracionTest {

    @Autowired
    private ControladorValoracion controlador;

    @Autowired
    private ServicioValoracion servicioValoracion;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Test
    @DisplayName("POST /api/valoraciones OK: crea una valoracion correctamente")
    void testValorarPelicula() {
        crearUsuario("AnaValoradora", "ana@test.com");
        Authentication auth = new UsernamePasswordAuthenticationToken("AnaValoradora", null);
        
        DContenidoListaNuevo contenido = new DContenidoListaNuevo(1001L, TipoContenido.PELICULA, "Dune", "/img.jpg", LocalDate.now(), "Sinopsis", "url", null, null, null);
        DValoracionNueva dto = new DValoracionNueva(contenido, 5, "Excelente pelicula");
        var respuesta = controlador.valorar(auth, dto);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().puntuacion()).isEqualTo(5);
        assertThat(respuesta.getBody().comentario()).isEqualTo("Excelente pelicula");
    }

    @Test
    @DisplayName("GET /api/peliculas/{id}/valoraciones/media OK: obtiene la media correcta")
    void testObtenerMedia() {
        crearUsuario("PedroMedia", "pedro@test.com");
        crearUsuario("LuciaMedia", "lucia@test.com");
        
        Pelicula p = new Pelicula();
        p.setTmdbId(2002L);
        p.setTitulo("Pelicula Media");
        p.setEnlace("http://test.com/2002");
        
        servicioValoracion.valorar("PedroMedia", p, 4, "Buena");
        servicioValoracion.valorar("LuciaMedia", p, 2, "Regular");
        var respuesta = controlador.obtenerMediaPelicula(2002L);
        assertThat(respuesta.media()).isEqualTo(3.0);
        assertThat(respuesta.total()).isEqualTo(2);
    }

    @Test
    @DisplayName("GET /api/series/{id}/valoraciones OK: lista las valoraciones de una serie")
    void testListarValoracionesSerie() {
        crearUsuario("CarlosSerie", "carlos@test.com");
        Serie s = new Serie();
        s.setTmdbId(3003L);
        s.setTitulo("Serie Carlos");
        s.setEnlace("http://test.com/3003");
        servicioValoracion.valorar("CarlosSerie", s, 5, "Obra maestra");
        var lista = controlador.obtenerValoracionesSerie(3003L);
        assertThat(lista).isNotEmpty();
        assertThat(lista.get(0).usuarioNombre()).isEqualTo("CarlosSerie");
    }

    private void crearUsuario(String nombre, String email) {
        Usuario u = new Usuario(null, nombre, "password", email, null, null, Tipousuario.USER, false);
        servicioUsuario.nuevoUsuario(u);
    }
}
