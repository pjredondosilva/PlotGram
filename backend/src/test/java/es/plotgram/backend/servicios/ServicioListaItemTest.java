package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.*;
import es.plotgram.backend.repositorios.RepositorioContenido;
import es.plotgram.backend.repositorios.RepositorioLista;
import es.plotgram.backend.repositorios.RepositorioUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
class ServicioListaItemTest {

    @Autowired
    private ServicioListaItem servicioListaItem;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private RepositorioLista repositorioLista;

    @Autowired
    private RepositorioContenido repositorioContenido;

    @Test
    @DisplayName("guardar OK: persiste un elemento de lista")
    void testGuardarListaItem() {
        DatosBase datos = datosBase("TaniaItemGuardar", "Lista guardar", 201L);
        ListaItem item = new ListaItem(null, datos.lista(), datos.contenido(), 0);

        ListaItem guardado = servicioListaItem.guardar(item);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getOrden()).isEqualTo(0);
        assertThat(guardado.getContenido().getTitulo()).isEqualTo("Contenido 201");
    }

    @Test
    @DisplayName("buscarPorListaIdOrdenados OK: devuelve los elementos ordenados por orden e id")
    void testBuscarPorListaIdOrdenados() {
        DatosBase datos = datosBase("UlisesItemOrden", "Lista orden", 202L);
        Contenido contenido2 = repositorioContenido.guardar(pelicula(203L));

        servicioListaItem.guardar(new ListaItem(null, datos.lista(), contenido2, 1));
        servicioListaItem.guardar(new ListaItem(null, datos.lista(), datos.contenido(), 0));

        var resultado = servicioListaItem.buscarPorListaIdOrdenados(datos.lista().getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(ListaItem::getOrden).containsExactly(0, 1);
        assertThat(resultado.get(0).getContenido().getTmdbId()).isEqualTo(202L);
        assertThat(resultado.get(1).getContenido().getTmdbId()).isEqualTo(203L);
    }

    @Test
    @DisplayName("contarPorListaId OK: devuelve el numero de elementos de una lista")
    void testContarPorListaId() {
        DatosBase datos = datosBase("VeraItemContar", "Lista contar", 204L);
        Contenido contenido2 = repositorioContenido.guardar(pelicula(205L));

        servicioListaItem.guardar(new ListaItem(null, datos.lista(), datos.contenido(), 0));
        servicioListaItem.guardar(new ListaItem(null, datos.lista(), contenido2, 1));

        Long total = servicioListaItem.contarPorListaId(datos.lista().getId());

        assertThat(total).isEqualTo(2L);
    }

    @Test
    @DisplayName("existePorListaIdYContenidoId OK: indica si el contenido ya esta en la lista")
    void testExistePorListaIdYContenidoId() {
        DatosBase datos = datosBase("WandaItemExiste", "Lista existe", 206L);
        servicioListaItem.guardar(new ListaItem(null, datos.lista(), datos.contenido(), 0));

        boolean existe = servicioListaItem.existePorListaIdYContenidoId(
                datos.lista().getId(),
                datos.contenido().getId()
        );

        boolean noExiste = servicioListaItem.existePorListaIdYContenidoId(
                datos.lista().getId(),
                999999L
        );

        assertThat(existe).isTrue();
        assertThat(noExiste).isFalse();
    }

    @Test
    @DisplayName("buscarUltimoOrdenDeLista OK: devuelve el mayor orden de la lista")
    void testBuscarUltimoOrdenDeLista() {
        DatosBase datos = datosBase("XaviItemUltimo", "Lista Ãºltimo", 207L);
        Contenido contenido2 = repositorioContenido.guardar(pelicula(208L));

        servicioListaItem.guardar(new ListaItem(null, datos.lista(), datos.contenido(), 0));
        servicioListaItem.guardar(new ListaItem(null, datos.lista(), contenido2, 5));

        Integer ultimoOrden = servicioListaItem.buscarUltimoOrdenDeLista(datos.lista().getId());

        assertThat(ultimoOrden).isEqualTo(5);
    }

    @Test
    @DisplayName("buscarPorIdYListaId OK: devuelve el elemento si pertenece a la lista")
    void testBuscarPorIdYListaId() {
        DatosBase datos = datosBase("YagoItemBuscar", "Lista buscar", 209L);
        ListaItem guardado = servicioListaItem.guardar(
                new ListaItem(null, datos.lista(), datos.contenido(), 0)
        );

        var resultado = servicioListaItem.buscarPorIdYListaId(
                guardado.getId(),
                datos.lista().getId()
        );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getContenido().getTmdbId()).isEqualTo(209L);
    }

    @Test
    @DisplayName("borrar OK: elimina el elemento de la lista")
    void testBorrarListaItem() {
        DatosBase datos = datosBase("ZoeItemBorrar", "Lista borrar", 210L);
        ListaItem guardado = servicioListaItem.guardar(
                new ListaItem(null, datos.lista(), datos.contenido(), 0)
        );

        servicioListaItem.borrar(guardado);

        assertThat(servicioListaItem.buscarPorIdYListaId(
                guardado.getId(),
                datos.lista().getId()
        )).isEmpty();

        assertThat(servicioListaItem.contarPorListaId(datos.lista().getId())).isZero();
    }

    private DatosBase datosBase(String nombreUsuario, String nombreLista, Long tmdbId) {
        Usuario usuario = repositorioUsuario.guardar(new Usuario(
                null,
                nombreUsuario,
                "hash",
                nombreUsuario.toLowerCase() + "@gmail.com",
                null,
                null,
                Tipousuario.USER,
                false
        ));

        Lista lista = repositorioLista.guardar(new Lista(
                null,
                nombreLista,
                "DescripciÃ³n",
                "/portada.jpg",
                usuario
        ));

        Contenido contenido = repositorioContenido.guardar(pelicula(tmdbId));

        return new DatosBase(usuario, lista, contenido);
    }

    private Pelicula pelicula(Long tmdbId) {
        Pelicula pelicula = new Pelicula();
        pelicula.setTmdbId(tmdbId);
        pelicula.setTitulo("Contenido " + tmdbId);
        pelicula.setImagen("/contenido-" + tmdbId + ".jpg");
        pelicula.setFechaPublicacion(LocalDate.of(2022, 3, 3));
        pelicula.setSinopsis("Sinopsis " + tmdbId);
        pelicula.setEnlace("https://plotgram.test/contenidos/" + tmdbId);
        return pelicula;
    }

    private record DatosBase(Usuario usuario, Lista lista, Contenido contenido) {
    }
}