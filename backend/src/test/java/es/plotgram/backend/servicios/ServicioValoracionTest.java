package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Pelicula;
import es.plotgram.backend.entidades.TipoContenido;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.entidades.Valoracion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

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
                "plotgram.admin.email=admin-test@plotgram.test"
        }
)
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
        // GIVEN
        crearUsuario("JuanValorar", "juan@test.com");
        Pelicula p = new Pelicula();
        p.setTmdbId(5005L);
        p.setTitulo("Pelicula Juan");
        p.setEnlace("http://test.com/5005");

        // WHEN
        Valoracion v = servicioValoracion.valorar("JuanValorar", p, 4, "Me gustó");

        // THEN
        assertThat(v.getId()).isNotNull();
        assertThat(v.getPuntuacion()).isEqualTo(4);
        assertThat(v.getUsuario().getNombre()).isEqualTo("JuanValorar");
    }

    @Test
    @DisplayName("obtenerMedia OK: calcula la media de varias valoraciones")
    void testCalcularMedia() {
        // GIVEN
        crearUsuario("User1Media", "u1@test.com");
        crearUsuario("User2Media", "u2@test.com");
        
        Pelicula p = new Pelicula();
        p.setTmdbId(6006L);
        p.setTitulo("Pelicula Media");
        p.setEnlace("http://test.com/6006");
        
        servicioValoracion.valorar("User1Media", p, 5, "Genial");
        servicioValoracion.valorar("User2Media", p, 3, "Meh");

        // WHEN
        Double media = servicioValoracion.obtenerMedia(6006L, TipoContenido.PELICULA);
        Long total = servicioValoracion.obtenerTotal(6006L, TipoContenido.PELICULA);

        // THEN
        assertThat(media).isEqualTo(4.0);
        assertThat(total).isEqualTo(2L);
    }

    private void crearUsuario(String nombre, String email) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setContrasena("password");
        servicioUsuario.nuevoUsuario(u);
    }
}
