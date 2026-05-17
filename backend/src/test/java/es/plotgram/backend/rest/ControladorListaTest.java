package es.plotgram.backend.rest;

import es.plotgram.backend.entidades.Pelicula;
import es.plotgram.backend.entidades.TipoContenido;
import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.rest.dto.DContenidoListaNuevo;
import es.plotgram.backend.rest.dto.DListaNueva;
import es.plotgram.backend.servicios.ServicioLista;
import es.plotgram.backend.servicios.ServicioUsuario;
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
                "gemini.api-key=test-key"
        }
)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ControladorListaTest {

    @Autowired
    private ControladorLista controlador;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Autowired
    private ServicioLista servicioLista;

    @Test
    @DisplayName("POST /api/usuarios/me/listas OK: crea una lista y devuelve 201")
    void testCrearLista() {
        crearUsuario("AliciaControladorLista", "alicia.controlador.lista@gmail.com");
        Authentication auth = auth("AliciaControladorLista");
        var dto = new DListaNueva("Favoritas Alicia", "DescripciÃ³n Alicia", "/alicia.jpg");

        var respuesta = controlador.crearLista(auth, dto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().nombre()).isEqualTo("Favoritas Alicia");
        assertThat(respuesta.getBody().descripcion()).isEqualTo("DescripciÃ³n Alicia");
        assertThat(respuesta.getBody().elementos()).isEmpty();
    }

    @Test
    @DisplayName("GET /api/usuarios/me/listas OK: devuelve las listas del usuario autenticado")
    void testObtenerMisListas() {
        crearUsuario("BorjaControladorMisListas", "borja.controlador.mislistas@gmail.com");
        Authentication auth = auth("BorjaControladorMisListas");
        controlador.crearLista(auth, new DListaNueva("Lista Borja 1", null, null));
        controlador.crearLista(auth, new DListaNueva("Lista Borja 2", null, null));

        var respuesta = controlador.obtenerMisListas(auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).hasSize(2);
        assertThat(respuesta.getBody()).extracting(resumen -> resumen.nombre())
                .containsExactly("Lista Borja 2", "Lista Borja 1");
    }

    @Test
    @DisplayName("PUT /api/usuarios/me/listas/{idLista} OK: edita una lista del usuario autenticado")
    void testEditarMiLista() {
        crearUsuario("CeliaControladorEditar", "celia.controlador.editar@gmail.com");
        Authentication auth = auth("CeliaControladorEditar");
        Long idLista = controlador.crearLista(auth, new DListaNueva("Original Celia", "Original", null)).getBody().id();

        var respuesta = controlador.editarMiLista(idLista, auth, new DListaNueva("Editada Celia", "Editada", "/celia-editada.jpg"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().nombre()).isEqualTo("Editada Celia");
        assertThat(respuesta.getBody().descripcion()).isEqualTo("Editada");
        assertThat(respuesta.getBody().imagenPortada()).isEqualTo("/celia-editada.jpg");
    }

    @Test
    @DisplayName("DELETE /api/usuarios/me/listas/{idLista} OK: elimina una lista del usuario autenticado")
    void testEliminarMiLista() {
        crearUsuario("DianaControladorEliminar", "diana.controlador.eliminar@gmail.com");
        Authentication auth = auth("DianaControladorEliminar");
        Long idLista = controlador.crearLista(auth, new DListaNueva("Lista Diana", null, null)).getBody().id();

        var respuesta = controlador.eliminarMiLista(idLista, auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(controlador.obtenerMisListas(auth).getBody()).isEmpty();
    }

    @Test
    @DisplayName("POST /api/usuarios/me/listas/{idLista}/elementos OK: aÃ±ade contenido a una lista")
    void testAniadirElemento() {
        crearUsuario("EvaControladorElemento", "eva.controlador.elemento@gmail.com");
        Authentication auth = auth("EvaControladorElemento");
        Long idLista = controlador.crearLista(auth, new DListaNueva("Lista Eva", null, null)).getBody().id();

        var respuesta = controlador.aniadirElemento(idLista, auth, contenido(401L, "Contenido Eva"));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().elementos()).hasSize(1);
        assertThat(respuesta.getBody().elementos().get(0).titulo()).isEqualTo("Contenido Eva");
        assertThat(respuesta.getBody().elementos().get(0).orden()).isEqualTo(0);
    }

    @Test
    @DisplayName("DELETE /api/usuarios/me/listas/{idLista}/elementos/{idElemento} OK: elimina un elemento")
    void testEliminarElemento() {
        Usuario usuario = crearUsuario("FerEliminarElemento", "fernando.controlador.eliminar.elemento@gmail.com");
        Authentication auth = auth("FerEliminarElemento");
        Long idLista = controlador.crearLista(auth, new DListaNueva("Lista Fernando", null, null)).getBody().id();
        Long idElemento = controlador.aniadirElemento(idLista, auth, contenido(402L, "Contenido Fernando"))
                .getBody().elementos().get(0).idItem();

        var respuesta = controlador.eliminarElemento(idLista, idElemento, auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(controlador.obtenerListaDeUsuario(usuario.getId(), idLista).getBody().elementos()).isEmpty();
    }

    @Test
    @DisplayName("GET /api/usuarios/{idUsuario}/listas OK: devuelve las listas de un usuario")
    void testObtenerListasDeUsuario() {
        Usuario usuario = crearUsuario("GonzaloControladorPublicas", "gonzalo.controlador.publicas@gmail.com");
        controlador.crearLista(auth("GonzaloControladorPublicas"), new DListaNueva("Lista PÃºblica Gonzalo", null, null));

        var respuesta = controlador.obtenerListasDeUsuario(usuario.getId());

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).hasSize(1);
        assertThat(respuesta.getBody().get(0).nombre()).isEqualTo("Lista PÃºblica Gonzalo");
    }

    @Test
    @DisplayName("GET /api/usuarios/{idUsuario}/listas/{idLista} OK: devuelve el detalle de una lista")
    void testObtenerListaDeUsuario() {
        Usuario usuario = crearUsuario("HelenaControladorDetalle", "helena.controlador.detalle@gmail.com");
        Long idLista = controlador.crearLista(auth("HelenaControladorDetalle"), new DListaNueva("Detalle Helena", null, null)).getBody().id();
        servicioLista.aniadirContenido(idLista, "HelenaControladorDetalle", pelicula(403L, "Contenido Helena"));

        var respuesta = controlador.obtenerListaDeUsuario(usuario.getId(), idLista);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().nombre()).isEqualTo("Detalle Helena");
        assertThat(respuesta.getBody().elementos()).hasSize(1);
        assertThat(respuesta.getBody().elementos().get(0).titulo()).isEqualTo("Contenido Helena");
    }

    private Usuario crearUsuario(String nombre, String email) {
        servicioUsuario.nuevoUsuario(new Usuario(null, nombre, "hash", email, null, null, Tipousuario.USER, false));
        return servicioUsuario.buscarUsuario(nombre).orElseThrow();
    }

    private Authentication auth(String nombre) {
        return new UsernamePasswordAuthenticationToken(nombre, null);
    }

    private DListaNueva lista(String nombre) {
        return new DListaNueva(nombre, null, null);
    }

    private DContenidoListaNuevo contenido(Long tmdbId, String titulo) {
        return new DContenidoListaNuevo(
                tmdbId,
                TipoContenido.PELICULA,
                titulo,
                "/contenido-" + tmdbId + ".jpg",
                LocalDate.of(2024, 2, 2),
                "Sinopsis " + titulo,
                "https://plotgram.test/contenidos/" + tmdbId,
                null,
                null,
                null
        );
    }

    private Pelicula pelicula(Long tmdbId, String titulo) {
        Pelicula pelicula = new Pelicula();
        pelicula.setTmdbId(tmdbId);
        pelicula.setTitulo(titulo);
        pelicula.setImagen("/pelicula-" + tmdbId + ".jpg");
        pelicula.setFechaPublicacion(LocalDate.of(2024, 2, 2));
        pelicula.setSinopsis("Sinopsis " + titulo);
        pelicula.setEnlace("https://plotgram.test/peliculas/" + tmdbId);
        return pelicula;
    }
}
