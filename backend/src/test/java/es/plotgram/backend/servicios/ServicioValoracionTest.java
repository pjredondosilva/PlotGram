package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Pelicula;
import es.plotgram.backend.entidades.Serie;
import es.plotgram.backend.entidades.TipoContenido;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.entidades.Valoracion;
import es.plotgram.backend.excepciones.UsuarioNoEncontrado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = es.plotgram.backend.app.BackendApplication.class, properties = {
        "app.auth.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.auth.jwt.clockSkewSeconds=60",
        "tmdb.base-url=http://localhost:1",
        "tmdb.token=test-token",
        "plotgram.admin.nombre=admin-test",
        "plotgram.admin.password=Admin123!",
        "plotgram.admin.email=admin-test@plotgram.test",
        "gemini.api-key=test-key"
})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ServicioValoracionTest {

    @Autowired
    private ServicioValoracion servicioValoracion;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Test
    @DisplayName("valorar OK: guarda una nueva valoración y permite recuperarla")
    void testValorarExito() {
        crearUsuario("JuanValorar", "juan@test.com");

        Pelicula pelicula = pelicula(5005L, "Pelicula Juan");

        Valoracion valoracion = servicioValoracion.valorar("JuanValorar", pelicula, 4, "Me gustó");

        assertThat(valoracion.getId()).isNotNull();
        assertThat(valoracion.getPuntuacion()).isEqualTo(4);
        assertThat(valoracion.getComentario()).isEqualTo("Me gustó");
        assertThat(valoracion.getUsuario().getNombre()).isEqualTo("JuanValorar");
        assertThat(valoracion.getContenido().getTmdbId()).isEqualTo(5005L);
    }

    @Test
    @DisplayName("valorar OK: actualiza la valoración si el usuario ya había valorado el mismo contenido")
    void testValorarActualizaExistente() {
        crearUsuario("AnaActualiza", "ana.actualiza@test.com");

        Pelicula pelicula = pelicula(5006L, "Pelicula Actualizable");

        Valoracion primera = servicioValoracion.valorar("AnaActualiza", pelicula, 2, "Regular");
        Valoracion actualizada = servicioValoracion.valorar("AnaActualiza", pelicula, 5, "Mucho mejor");

        assertThat(actualizada.getId()).isEqualTo(primera.getId());
        assertThat(actualizada.getPuntuacion()).isEqualTo(5);
        assertThat(actualizada.getComentario()).isEqualTo("Mucho mejor");

        var valoraciones = servicioValoracion.obtenerValoraciones(5006L, TipoContenido.PELICULA);
        assertThat(valoraciones).hasSize(1);
        assertThat(valoraciones.get(0).getPuntuacion()).isEqualTo(5);
    }

    @Test
    @DisplayName("valorar KO: lanza excepción si el usuario no existe")
    void testValorarUsuarioNoEncontrado() {
        Pelicula pelicula = pelicula(5007L, "Pelicula Sin Usuario");

        assertThatThrownBy(() -> servicioValoracion.valorar("UsuarioInexistente", pelicula, 3, "Comentario"))
                .isInstanceOf(UsuarioNoEncontrado.class);
    }

    @Test
    @DisplayName("obtenerValoraciones OK: devuelve las valoraciones de una película")
    void testObtenerValoracionesPelicula() {
        crearUsuario("LauraValoracion", "laura.valoracion@test.com");
        crearUsuario("MarioValoracion", "mario.valoracion@test.com");

        Pelicula pelicula = pelicula(6001L, "Pelicula Valoraciones");

        servicioValoracion.valorar("LauraValoracion", pelicula, 5, "Excelente");
        servicioValoracion.valorar("MarioValoracion", pelicula, 3, "Correcta");

        var valoraciones = servicioValoracion.obtenerValoraciones(6001L, TipoContenido.PELICULA);

        assertThat(valoraciones).hasSize(2);
        assertThat(valoraciones).extracting(Valoracion::getComentario)
                .containsExactly("Correcta", "Excelente");
    }

    @Test
    @DisplayName("obtenerValoraciones OK: devuelve las valoraciones de una serie")
    void testObtenerValoracionesSerie() {
        crearUsuario("CarlosSerieServicio", "carlos.serie.servicio@test.com");

        Serie serie = serie(7001L, "Serie Servicio");

        servicioValoracion.valorar("CarlosSerieServicio", serie, 4, "Muy buena");

        var valoraciones = servicioValoracion.obtenerValoraciones(7001L, TipoContenido.SERIE);

        assertThat(valoraciones).hasSize(1);
        assertThat(valoraciones.get(0).getUsuario().getNombre()).isEqualTo("CarlosSerieServicio");
        assertThat(valoraciones.get(0).getPuntuacion()).isEqualTo(4);
        assertThat(valoraciones.get(0).getComentario()).isEqualTo("Muy buena");
    }

    @Test
    @DisplayName("obtenerMedia OK: calcula la media de varias valoraciones")
    void testCalcularMedia() {
        crearUsuario("User1Media", "u1@test.com");
        crearUsuario("User2Media", "u2@test.com");

        Pelicula pelicula = pelicula(6006L, "Pelicula Media");

        servicioValoracion.valorar("User1Media", pelicula, 5, "Genial");
        servicioValoracion.valorar("User2Media", pelicula, 3, "Meh");

        Double media = servicioValoracion.obtenerMedia(6006L, TipoContenido.PELICULA);
        Long total = servicioValoracion.obtenerTotal(6006L, TipoContenido.PELICULA);

        assertThat(media).isEqualTo(4.0);
        assertThat(total).isEqualTo(2L);
    }

    @Test
    @DisplayName("obtenerMedia OK: devuelve 0.0 si no existen valoraciones")
    void testObtenerMediaSinValoraciones() {
        Double media = servicioValoracion.obtenerMedia(9999L, TipoContenido.PELICULA);

        assertThat(media).isEqualTo(0.0);
    }

    @Test
    @DisplayName("obtenerTotal OK: devuelve cero si no existen valoraciones")
    void testObtenerTotalSinValoraciones() {
        long total = servicioValoracion.obtenerTotal(9999L, TipoContenido.SERIE);

        assertThat(total).isZero();
    }

    @Test
    @DisplayName("obtenerMedia OK: calcula la media de una serie")
    void testCalcularMediaSerie() {
        crearUsuario("SerieMedia1", "serie.media1@test.com");
        crearUsuario("SerieMedia2", "serie.media2@test.com");

        Serie serie = serie(8008L, "Serie Media Servicio");

        servicioValoracion.valorar("SerieMedia1", serie, 4, "Buena");
        servicioValoracion.valorar("SerieMedia2", serie, 2, "Regular");

        Double media = servicioValoracion.obtenerMedia(8008L, TipoContenido.SERIE);
        long total = servicioValoracion.obtenerTotal(8008L, TipoContenido.SERIE);

        assertThat(media).isEqualTo(3.0);
        assertThat(total).isEqualTo(2L);
    }

    private void crearUsuario(String nombre, String email) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setContrasena("password");
        servicioUsuario.nuevoUsuario(usuario);
    }

    private Pelicula pelicula(Long tmdbId, String titulo) {
        Pelicula pelicula = new Pelicula();
        pelicula.setTmdbId(tmdbId);
        pelicula.setTitulo(titulo);
        pelicula.setEnlace("http://test.com/peliculas/" + tmdbId);
        return pelicula;
    }

    private Serie serie(Long tmdbId, String titulo) {
        Serie serie = new Serie();
        serie.setTmdbId(tmdbId);
        serie.setTitulo(titulo);
        serie.setEnlace("http://test.com/series/" + tmdbId);
        return serie;
    }
}