package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.*;
import es.plotgram.backend.excepciones.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(
        classes = es.plotgram.backend.app.BackendApplication.class,
        properties = {
                "app.auth.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "app.auth.jwt.clockSkewSeconds=60",
                "tmdb.base-url=http://localhost:1",
                "tmdb.token=test-token",
                "plotgram.admin.nombre=admin-test",
                "plotgram.admin.password=Admin123!",
                "plotgram.admin.email=admin-test@plotgram.test"
        }
)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ServicioListaTest {

    @Autowired
    private ServicioLista servicioLista;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Test
    @DisplayName("crearLista OK: crea una lista vacía para el usuario")
    void testCrearListaValida() {
        crearUsuario("AnaListaCrear", "ana.lista.crear@gmail.com");
        Lista listaNueva = lista("Favoritas Ana", "Películas favoritas", "/ana.jpg");

        var resultado = servicioLista.crearLista("AnaListaCrear", listaNueva);

        assertThat(resultado.lista().getId()).isNotNull();
        assertThat(resultado.lista().getNombre()).isEqualTo("Favoritas Ana");
        assertThat(resultado.lista().getUsuario().getNombre()).isEqualTo("AnaListaCrear");
        assertThat(resultado.elementos()).isEmpty();
    }

    @Test
    @DisplayName("crearLista KO: no permite repetir nombre de lista para el mismo usuario")
    void testCrearListaNombreRepetido() {
        crearUsuario("BertoListaDuplicada", "berto.lista.duplicada@gmail.com");
        servicioLista.crearLista("BertoListaDuplicada", lista("Pendientes Berto", null, null));

        assertThatThrownBy(() -> servicioLista.crearLista("BertoListaDuplicada", lista(" pendientes berto ", null, null)))
                .isInstanceOf(ListaYaRegistrada.class)
                .hasMessageContaining("lista");
    }

    @Test
    @DisplayName("crearLista KO: no permite crear una lista con nombre en blanco")
    void testCrearListaNombreEnBlanco() {
        crearUsuario("CarlaListaBlanco", "carla.lista.blanco@gmail.com");

        assertThatThrownBy(() -> servicioLista.crearLista("CarlaListaBlanco", lista("   ", null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("El nombre de la lista es obligatorio");
    }

    @Test
    @DisplayName("obtenerMisListas OK: devuelve los resúmenes con el número de elementos")
    void testObtenerMisListas() {
        crearUsuario("DarioMisListas", "dario.mis.listas@gmail.com");
        var lista1 = servicioLista.crearLista("DarioMisListas", lista("Vistas Dario", null, null)).lista();
        servicioLista.crearLista("DarioMisListas", lista("Pendientes Dario", null, null));
        servicioLista.aniadirContenido(lista1.getId(), "DarioMisListas", pelicula(301L, "Contenido Dario"));

        var resultado = servicioLista.obtenerMisListas("DarioMisListas");

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(resumen -> resumen.lista().getNombre())
                .containsExactly("Pendientes Dario", "Vistas Dario");
        assertThat(resultado.stream()
                .filter(resumen -> resumen.lista().getNombre().equals("Vistas Dario"))
                .findFirst().orElseThrow().totalElementos()).isEqualTo(1L);
    }

    @Test
    @DisplayName("obtenerListasDeUsuario OK: devuelve las listas del usuario indicado")
    void testObtenerListasDeUsuario() {
        Usuario usuario = crearUsuario("ElenaListasPublicas", "elena.listas.publicas@gmail.com");
        servicioLista.crearLista("ElenaListasPublicas", lista("Series Elena", null, null));

        var resultado = servicioLista.obtenerListasDeUsuario(usuario.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).lista().getNombre()).isEqualTo("Series Elena");
    }

    @Test
    @DisplayName("obtenerListaDeUsuario OK: devuelve la lista con sus elementos")
    void testObtenerListaDeUsuario() {
        Usuario usuario = crearUsuario("FedeListaDetalle", "fede.lista.detalle@gmail.com");
        var lista = servicioLista.crearLista("FedeListaDetalle", lista("Detalle Fede", null, null)).lista();
        servicioLista.aniadirContenido(lista.getId(), "FedeListaDetalle", pelicula(302L, "Contenido Fede"));

        var resultado = servicioLista.obtenerListaDeUsuario(usuario.getId(), lista.getId());

        assertThat(resultado.lista().getNombre()).isEqualTo("Detalle Fede");
        assertThat(resultado.elementos()).hasSize(1);
        assertThat(resultado.elementos().get(0).getContenido().getTitulo()).isEqualTo("Contenido Fede");
    }

    @Test
    @DisplayName("aniadirContenido OK: añade contenido nuevo con orden inicial cero")
    void testAniadirContenidoPrimero() {
        crearUsuario("GalaListaContenido", "gala.lista.contenido@gmail.com");
        var lista = servicioLista.crearLista("GalaListaContenido", lista("Lista Gala", null, null)).lista();

        var resultado = servicioLista.aniadirContenido(lista.getId(), "GalaListaContenido", pelicula(303L, "Contenido Gala"));

        assertThat(resultado.elementos()).hasSize(1);
        assertThat(resultado.elementos().get(0).getOrden()).isEqualTo(0);
        assertThat(resultado.elementos().get(0).getContenido().getTitulo()).isEqualTo("Contenido Gala");
    }

    @Test
    @DisplayName("aniadirContenido OK: añade el siguiente contenido con orden consecutivo")
    void testAniadirContenidoOrdenConsecutivo() {
        crearUsuario("HectorListaOrden", "hector.lista.orden@gmail.com");
        var lista = servicioLista.crearLista("HectorListaOrden", lista("Lista Hector", null, null)).lista();
        servicioLista.aniadirContenido(lista.getId(), "HectorListaOrden", pelicula(304L, "Contenido Hector 1"));

        var resultado = servicioLista.aniadirContenido(lista.getId(), "HectorListaOrden", pelicula(305L, "Contenido Hector 2"));

        assertThat(resultado.elementos()).hasSize(2);
        assertThat(resultado.elementos()).extracting(ListaItem::getOrden).containsExactly(0, 1);
    }

    @Test
    @DisplayName("aniadirContenido KO: no permite añadir dos veces el mismo contenido a la misma lista")
    void testAniadirContenidoDuplicado() {
        crearUsuario("InesListaDuplicada", "ines.lista.duplicada@gmail.com");
        var lista = servicioLista.crearLista("InesListaDuplicada", lista("Lista Ines", null, null)).lista();
        servicioLista.aniadirContenido(lista.getId(), "InesListaDuplicada", pelicula(306L, "Contenido Ines"));

        assertThatThrownBy(() -> servicioLista.aniadirContenido(lista.getId(), "InesListaDuplicada", pelicula(306L, "Contenido Ines Repetido")))
                .isInstanceOf(ContenidoYaEnLista.class)
                .hasMessageContaining("contenido");
    }

    @Test
    @DisplayName("editarLista OK: actualiza nombre, descripción e imagen sin eliminar elementos")
    void testEditarListaValida() {
        crearUsuario("JorgeListaEditar", "jorge.lista.editar@gmail.com");
        var lista = servicioLista.crearLista("JorgeListaEditar", lista("Original Jorge", "Original", "/original.jpg")).lista();
        servicioLista.aniadirContenido(lista.getId(), "JorgeListaEditar", pelicula(307L, "Contenido Jorge"));

        var resultado = servicioLista.editarLista(lista.getId(), "JorgeListaEditar", lista("Editada Jorge", "Editada", "/editada.jpg"));

        assertThat(resultado.lista().getNombre()).isEqualTo("Editada Jorge");
        assertThat(resultado.lista().getDescripcion()).isEqualTo("Editada");
        assertThat(resultado.lista().getImagenPortada()).isEqualTo("/editada.jpg");
        assertThat(resultado.elementos()).hasSize(1);
    }

    @Test
    @DisplayName("editarLista KO: no permite cambiar a un nombre ya usado por otra lista del usuario")
    void testEditarListaNombreRepetido() {
        crearUsuario("KarenListaEditar", "karen.lista.editar@gmail.com");
        servicioLista.crearLista("KarenListaEditar", lista("Nombre Ocupado Karen", null, null));
        var listaEditar = servicioLista.crearLista("KarenListaEditar", lista("Nombre Libre Karen", null, null)).lista();

        assertThatThrownBy(() -> servicioLista.editarLista(listaEditar.getId(), "KarenListaEditar", lista("Nombre Ocupado Karen", null, null)))
                .isInstanceOf(ListaYaRegistrada.class);
    }

    @Test
    @DisplayName("eliminarElemento OK: borra un elemento concreto de la lista")
    void testEliminarElemento() {
        crearUsuario("LuisListaEliminarElemento", "luis.lista.eliminar.elemento@gmail.com");
        var lista = servicioLista.crearLista("LuisListaEliminarElemento", lista("Lista Luis", null, null)).lista();
        var detalle = servicioLista.aniadirContenido(lista.getId(), "LuisListaEliminarElemento", pelicula(308L, "Contenido Luis"));
        Long idItem = detalle.elementos().get(0).getId();

        servicioLista.eliminarElemento(lista.getId(), idItem, "LuisListaEliminarElemento");

        var resultado = servicioLista.obtenerListaDeUsuario(servicioUsuario.buscarUsuario("LuisListaEliminarElemento").orElseThrow().getId(), lista.getId());
        assertThat(resultado.elementos()).isEmpty();
    }

    @Test
    @DisplayName("eliminarElemento KO: lanza excepción si el elemento no pertenece a la lista")
    void testEliminarElementoNoEncontrado() {
        crearUsuario("MartaListaElementoNoExiste", "marta.lista.elemento.noexiste@gmail.com");
        var lista = servicioLista.crearLista("MartaListaElementoNoExiste", lista("Lista Marta", null, null)).lista();

        assertThatThrownBy(() -> servicioLista.eliminarElemento(lista.getId(), 999999L, "MartaListaElementoNoExiste"))
                .isInstanceOf(ElementoNoEncontradoEnLista.class);
    }

    @Test
    @DisplayName("eliminarLista OK: borra la lista y sus elementos")
    void testEliminarLista() {
        Usuario usuario = crearUsuario("NicoListaEliminar", "nico.lista.eliminar@gmail.com");
        var lista = servicioLista.crearLista("NicoListaEliminar", lista("Lista Nico", null, null)).lista();
        servicioLista.aniadirContenido(lista.getId(), "NicoListaEliminar", pelicula(309L, "Contenido Nico"));

        servicioLista.eliminarLista(lista.getId(), "NicoListaEliminar");

        assertThatThrownBy(() -> servicioLista.obtenerListaDeUsuario(usuario.getId(), lista.getId()))
                .isInstanceOf(ListaNoEncontrada.class);
    }

    @Test
    @DisplayName("obtenerMisListas KO: lanza excepción si el usuario no existe")
    void testObtenerMisListasUsuarioNoExiste() {
        assertThatThrownBy(() -> servicioLista.obtenerMisListas("UsuarioListaInexistente"))
                .isInstanceOf(UsuarioNoEncontrado.class);
    }

    private Usuario crearUsuario(String nombre, String email) {
        Usuario usuario = new Usuario(null, nombre, "hash", email, null, null, Tipousuario.USER, false);
        servicioUsuario.nuevoUsuario(usuario);
        return servicioUsuario.buscarUsuario(nombre).orElseThrow();
    }

    private Lista lista(String nombre, String descripcion, String imagenPortada) {
        Lista lista = new Lista();
        lista.setNombre(nombre);
        lista.setDescripcion(descripcion);
        lista.setImagenPortada(imagenPortada);
        return lista;
    }

    private Pelicula pelicula(Long tmdbId, String titulo) {
        Pelicula pelicula = new Pelicula();
        pelicula.setTmdbId(tmdbId);
        pelicula.setTitulo(titulo);
        pelicula.setImagen("/pelicula-" + tmdbId + ".jpg");
        pelicula.setFechaPublicacion(LocalDate.of(2024, 1, 1));
        pelicula.setSinopsis("Sinopsis " + titulo);
        pelicula.setEnlace("https://plotgram.test/peliculas/" + tmdbId);
        return pelicula;
    }
}
