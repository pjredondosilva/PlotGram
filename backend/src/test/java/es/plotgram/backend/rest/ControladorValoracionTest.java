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

@SpringBootTest(classes = es.plotgram.backend.app.BackendApplication.class, properties = {
        "app.auth.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.auth.jwt.clockSkewSeconds=60",
        "tmdb.base-url=http://localhost:1",
        "tmdb.token=test-token",
        "plotgram.admin.nombre=admin-test",
        "plotgram.admin.password=Admin123!",
        "plotgram.admin.email=admin-test@plotgram.test",
        "gemini.api-key=test-key",
        "gemini.model=gemini-2.5-flash"
})
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

        DContenidoListaNuevo contenido = new DContenidoListaNuevo(1001L, TipoContenido.PELICULA, "Dune", "/img.jpg",
                LocalDate.now(), "Sinopsis", "url", null, null, null);
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

        Authentication authPedro = new UsernamePasswordAuthenticationToken("PedroMedia", null);
        Authentication authLucia = new UsernamePasswordAuthenticationToken("LuciaMedia", null);

        DContenidoListaNuevo contenido = new DContenidoListaNuevo(2002L, TipoContenido.PELICULA, "Pelicula Media", "/img.jpg", LocalDate.now(), "Sinopsis", "http://test.com/2002", null, null, null);

        controlador.valorar(authPedro, new DValoracionNueva(contenido, 4, "Buena"));
        controlador.valorar(authLucia, new DValoracionNueva(contenido, 2, "Regular"));

        var respuesta = controlador.obtenerMediaPelicula(2002L);
        assertThat(respuesta.media()).isEqualTo(3.0);
        assertThat(respuesta.total()).isEqualTo(2);
    }

    @Test
    @DisplayName("GET /api/series/{id}/valoraciones OK: lista las valoraciones de una serie")
    void testListarValoracionesSerie() {
        crearUsuario("CarlosSerie", "carlos@test.com");
        Authentication authCarlos = new UsernamePasswordAuthenticationToken("CarlosSerie", null);

        DContenidoListaNuevo contenido = new DContenidoListaNuevo(3003L, TipoContenido.SERIE, "Serie Carlos", "/img.jpg", LocalDate.now(), "Sinopsis", "http://test.com/3003", null, null, null);
        
        controlador.valorar(authCarlos, new DValoracionNueva(contenido, 5, "Obra maestra"));

        var lista = controlador.obtenerValoracionesSerie(3003L);
        assertThat(lista).isNotEmpty();
        assertThat(lista.get(0).usuarioNombre()).isEqualTo("CarlosSerie");
    }

    @Test
    @DisplayName("GET /api/peliculas/{id}/valoraciones OK: lista las valoraciones de una pelicula")
    void testListarValoracionesPelicula() {
        crearUsuario("MartaPelicula", "marta@test.com");
        Authentication authMarta = new UsernamePasswordAuthenticationToken("MartaPelicula", null);

        DContenidoListaNuevo contenido = new DContenidoListaNuevo(4004L, TipoContenido.PELICULA, "Pelicula Marta", "/img.jpg", LocalDate.now(), "Sinopsis", "http://test.com/4004", null, null, null);
        
        controlador.valorar(authMarta, new DValoracionNueva(contenido, 5, "Me encanto"));

        var lista = controlador.obtenerValoracionesPelicula(4004L);
        assertThat(lista).isNotEmpty();
        assertThat(lista.get(0).usuarioNombre()).isEqualTo("MartaPelicula");
        assertThat(lista.get(0).comentario()).isEqualTo("Me encanto");
    }

    @Test
    @DisplayName("GET /api/series/{id}/valoraciones/media OK: obtiene la media correcta de una serie")
    void testObtenerMediaSerie() {
        crearUsuario("JuanSerie", "juan@test.com");
        crearUsuario("ElenaSerie", "elena@test.com");

        Authentication authJuan = new UsernamePasswordAuthenticationToken("JuanSerie", null);
        Authentication authElena = new UsernamePasswordAuthenticationToken("ElenaSerie", null);

        DContenidoListaNuevo contenido = new DContenidoListaNuevo(5005L, TipoContenido.SERIE, "Serie Media", "/img.jpg", LocalDate.now(), "Sinopsis", "http://test.com/5005", null, null, null);

        controlador.valorar(authJuan, new DValoracionNueva(contenido, 5, "Fantastica"));
        controlador.valorar(authElena, new DValoracionNueva(contenido, 3, "Normal"));

        var respuesta = controlador.obtenerMediaSerie(5005L);
        assertThat(respuesta.media()).isEqualTo(4.0);
        assertThat(respuesta.total()).isEqualTo(2);
    }

    @Test
    @DisplayName("POST /api/valoraciones KO: lanza UsuarioNoEncontrado si el usuario no existe")
    void testValorarUsuarioNoEncontrado() {
        Authentication authFalso = new UsernamePasswordAuthenticationToken("UsuarioFantasma", null);
        DContenidoListaNuevo contenido = new DContenidoListaNuevo(1001L, TipoContenido.PELICULA, "Dune", "/img.jpg", LocalDate.now(), "Sinopsis", "url", null, null, null);
        DValoracionNueva dto = new DValoracionNueva(contenido, 5, "Excelente");

        org.junit.jupiter.api.Assertions.assertThrows(
                es.plotgram.backend.excepciones.UsuarioNoEncontrado.class,
                () -> controlador.valorar(authFalso, dto)
        );
    }

    private void crearUsuario(String nombre, String email) {
        Usuario u = new Usuario(null, nombre, "password", email, null, null, Tipousuario.USER, false);
        servicioUsuario.nuevoUsuario(u);
    }
}