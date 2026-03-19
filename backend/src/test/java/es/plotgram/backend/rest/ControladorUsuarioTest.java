package es.plotgram.backend.rest;

import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.rest.dto.DUsuarioRegistro;
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

import static org.assertj.core.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = es.plotgram.backend.app.BackendApplication.class)
@ActiveProfiles("test")
public class ControladorUsuarioTest {

    @Autowired
    private ControladorUsuario controlador;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Test
    @DisplayName("POST /api/usuarios OK: registra un usuario nuevo y devuelve 201")
    void testNuevoUsuarioValido() {
        var dto = new DUsuarioRegistro("Manolo", "1234", "manolo@gmail.com");

        var respuesta = controlador.nuevoUsuario(dto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(servicioUsuario.buscarUsuario("Manolo")).isPresent();
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} OK: devuelve el usuario si existe")
    void testObtenerUsuarioExistente() {
        var usuario = new Usuario(null, "Pedro", "1234", "pedro@gmail.com", Tipousuario.USER, false);
        servicioUsuario.nuevoUsuario(usuario);

        var usuarioGuardado = servicioUsuario.buscarUsuario("Pedro").orElseThrow();

        var respuesta = controlador.obtenerUsuario(usuarioGuardado.getId());

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().nombre()).isEqualTo("Pedro");
        assertThat(respuesta.getBody().email()).isEqualTo("pedro@gmail.com");
        assertThat(respuesta.getBody().tipo()).isEqualTo(Tipousuario.USER);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} KO: devuelve 404 si el usuario no existe")
    void testObtenerUsuarioNoExistente() {
        var respuesta = controlador.obtenerUsuario(999L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isNull();
    }

    @Test
    @DisplayName("GET /api/usuarios/me OK: devuelve el usuario autenticado si existe")
    void testMeExistente() {
        var usuario = new Usuario(null, "Pepe", "1234", "pepe@gmail.com", Tipousuario.USER, false);
        servicioUsuario.nuevoUsuario(usuario);

        Authentication auth = new UsernamePasswordAuthenticationToken("Pepe", null);

        var respuesta = controlador.me(auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().nombre()).isEqualTo("Pepe");
        assertThat(respuesta.getBody().email()).isEqualTo("pepe@gmail.com");
        assertThat(respuesta.getBody().tipo()).isEqualTo(Tipousuario.USER);
    }

    @Test
    @DisplayName("GET /api/usuarios/me KO: devuelve 404 si el usuario autenticado no existe")
    void testMeNoExistente() {
        Authentication auth = new UsernamePasswordAuthenticationToken("UsuarioInventado", null);

        var respuesta = controlador.me(auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isNull();
    }
}