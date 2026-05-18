package es.plotgram.backend.rest;

import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.rest.dto.DActualizacionPerfil;
import es.plotgram.backend.rest.dto.DUsuarioRegistro;
import es.plotgram.backend.rest.dto.DVerificacionContrasena;
import es.plotgram.backend.servicios.ServicioUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                "plotgram.admin.email=admin-test@plotgram.test",
                "gemini.api-key=test-key"
        }
)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ControladorUsuarioTest {

    @Autowired
    private ControladorUsuario controlador;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /api/usuarios OK: registra un usuario nuevo y devuelve 201")
    void testNuevoUsuarioValido() {
        var dto = new DUsuarioRegistro("NoraRegistro", "ClaveNora1!", "nora.registro@gmail.com");

        var respuesta = controlador.nuevoUsuario(dto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var usuarioGuardado = servicioUsuario.buscarUsuario("NoraRegistro").orElseThrow();
        assertThat(usuarioGuardado.getEmail()).isEqualTo("nora.registro@gmail.com");
        assertThat(usuarioGuardado.getContrasena()).isNotEqualTo("ClaveNora1!");
        assertThat(passwordEncoder.matches("ClaveNora1!", usuarioGuardado.getContrasena())).isTrue();
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} OK: devuelve el usuario si existe")
    void testObtenerUsuarioExistente() {
        servicioUsuario.nuevoUsuario(usuario("OscarConsulta", "hash-oscar", "oscar.consulta@gmail.com"));
        Long id = servicioUsuario.buscarUsuario("OscarConsulta").orElseThrow().getId();

        var respuesta = controlador.obtenerUsuario(id, null);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().nombre()).isEqualTo("OscarConsulta");
        assertThat(respuesta.getBody().email()).isEqualTo("oscar.consulta@gmail.com");
        assertThat(respuesta.getBody().contrasenia()).isNull();
        assertThat(respuesta.getBody().tipo()).isEqualTo(Tipousuario.USER);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} KO: devuelve 404 si el usuario no existe")
    void testObtenerUsuarioNoExistente() {
        var respuesta = controlador.obtenerUsuario(999999L, null);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isNull();
    }

    @Test
    @DisplayName("GET /api/usuarios/me OK: devuelve el usuario autenticado si existe")
    void testMeExistente() {
        servicioUsuario.nuevoUsuario(usuario("PaulaMe", "hash-paula", "paula.me@gmail.com"));
        Authentication auth = new UsernamePasswordAuthenticationToken("PaulaMe", null);

        var respuesta = controlador.me(auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().nombre()).isEqualTo("PaulaMe");
        assertThat(respuesta.getBody().email()).isEqualTo("paula.me@gmail.com");
    }

    @Test
    @DisplayName("GET /api/usuarios/me KO: devuelve 404 si el usuario autenticado no existe")
    void testMeNoExistente() {
        Authentication auth = new UsernamePasswordAuthenticationToken("UsuarioInventadoMe", null);

        var respuesta = controlador.me(auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isNull();
    }

    @Test
    @DisplayName("POST /api/usuarios/me/verificacioncontrasena OK: devuelve 204 si la contraseña es correcta")
    void testVerificarContrasenaCorrecta() {
        servicioUsuario.nuevoUsuario(usuario("RaquelPassword", passwordEncoder.encode("ClaveRaquel1!"), "raquel.password@gmail.com"));
        Authentication auth = new UsernamePasswordAuthenticationToken("RaquelPassword", null);
        var dto = new DVerificacionContrasena("ClaveRaquel1!");

        var respuesta = controlador.verificarContrasena(auth, dto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("PUT /api/usuarios/me/actualizacionperfil OK: actualiza el perfil y devuelve el usuario actualizado")
    void testActualizarPerfilValido() {
        servicioUsuario.nuevoUsuario(usuario("SergioPerfil", passwordEncoder.encode("ClaveSergio1!"), "sergio.perfil@gmail.com"));
        Authentication auth = new UsernamePasswordAuthenticationToken("SergioPerfil", null);
        var dto = new DActualizacionPerfil(
                " SergioNuevo ",
                " sergio.nuevo@gmail.com ",
                " https://imagenes.test/sergio.jpg ",
                " Nueva descripcion ",
                "ClaveSergio1!",
                "NuevaSergio1!"
        );

        var respuesta = controlador.actualizarPerfil(auth, dto);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().nombre()).isEqualTo("SergioNuevo");
        assertThat(respuesta.getBody().email()).isEqualTo("sergio.nuevo@gmail.com");
        assertThat(respuesta.getBody().fotoPerfil()).isEqualTo("https://imagenes.test/sergio.jpg");
        assertThat(respuesta.getBody().descripcion()).isEqualTo("Nueva descripcion");
        assertThat(respuesta.getBody().contrasenia()).isNull();
    }

    @Test
    @DisplayName("GET /api/usuarios/busqueda OK: devuelve la lista de usuarios que coinciden")
    void testBuscarUsuarios() {
        servicioUsuario.nuevoUsuario(usuario("ZeldaBusqueda1", "hash", "zelda1@gmail.com"));
        servicioUsuario.nuevoUsuario(usuario("ZeldaBusqueda2", "hash", "zelda2@gmail.com"));

        var respuesta = controlador.buscarUsuarios("ZeldaBusq");

        assertThat(respuesta).hasSize(2);
        assertThat(respuesta).extracting(es.plotgram.backend.rest.dto.DUsuario::nombre)
                .containsExactlyInAnyOrder("ZeldaBusqueda1", "ZeldaBusqueda2");
    }

    @Test
    @DisplayName("POST /api/usuarios/{id}/seguidores OK: sigue a un usuario y devuelve 204")
    void testSeguirUsuario() {
        servicioUsuario.nuevoUsuario(usuario("PacoSeguidor", "hash", "paco.seguidor@gmail.com"));
        servicioUsuario.nuevoUsuario(usuario("MariaSeguida", "hash", "maria.seguida@gmail.com"));
        Authentication auth = new UsernamePasswordAuthenticationToken("PacoSeguidor", null);
        Long idSeguido = servicioUsuario.buscarUsuario("MariaSeguida").orElseThrow().getId();

        var respuesta = controlador.seguir(idSeguido, auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(servicioUsuario.esSeguidor("PacoSeguidor", idSeguido)).isTrue();
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id}/seguidores OK: deja de seguir a un usuario y devuelve 204")
    void testDejarDeSeguirUsuario() {
        servicioUsuario.nuevoUsuario(usuario("AnaSeguidora", "hash", "ana.seguidora@gmail.com"));
        servicioUsuario.nuevoUsuario(usuario("LuisSeguido", "hash", "luis.seguido@gmail.com"));
        Authentication auth = new UsernamePasswordAuthenticationToken("AnaSeguidora", null);
        Long idSeguido = servicioUsuario.buscarUsuario("LuisSeguido").orElseThrow().getId();
        servicioUsuario.seguir(idSeguido, "AnaSeguidora");

        var respuesta = controlador.dejarDeSeguir(idSeguido, auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(servicioUsuario.esSeguidor("AnaSeguidora", idSeguido)).isFalse();
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} OK: devuelve loSigo=true si el usuario autenticado lo sigue")
    void testObtenerUsuarioExistenteConAutenticacionYSeguimiento() {
        servicioUsuario.nuevoUsuario(usuario("OscarAutenticado", "hash", "oscar.auth@gmail.com"));
        servicioUsuario.nuevoUsuario(usuario("PedroConsultado", "hash", "pedro.cons@gmail.com"));
        Long idConsultado = servicioUsuario.buscarUsuario("PedroConsultado").orElseThrow().getId();

        servicioUsuario.seguir(idConsultado, "OscarAutenticado");
        Authentication auth = new UsernamePasswordAuthenticationToken("OscarAutenticado", null, java.util.Collections.emptyList());

        var respuesta = controlador.obtenerUsuario(idConsultado, auth);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().nombre()).isEqualTo("PedroConsultado");
        assertThat(respuesta.getBody().loSigo()).isTrue();
    }

    @Test
    @DisplayName("POST /api/usuarios KO: lanza UsuarioYaRegistrado si el email o nombre ya existe")
    void testNuevoUsuarioDuplicado() {
        servicioUsuario.nuevoUsuario(usuario("Duplicado", "hash", "duplicado@gmail.com"));
        DUsuarioRegistro dto = new DUsuarioRegistro("Duplicado", "duplicado2@gmail.com", "Password123!");
        
        org.junit.jupiter.api.Assertions.assertThrows(
                es.plotgram.backend.excepciones.UsuarioYaRegistrado.class,
                () -> controlador.nuevoUsuario(dto)
        );
    }

    @Test
    @DisplayName("POST /api/usuarios/me/verificacioncontrasena KO: lanza ContrasenaActualIncorrecta si no coincide")
    void testVerificarContrasenaIncorrecta() {
        servicioUsuario.nuevoUsuario(usuario("UsuarioPassword", passwordEncoder.encode("Secreta123!"), "pwd@gmail.com"));
        Authentication auth = new UsernamePasswordAuthenticationToken("UsuarioPassword", null);
        DVerificacionContrasena dto = new DVerificacionContrasena("MalaContraseña!");

        org.junit.jupiter.api.Assertions.assertThrows(
                es.plotgram.backend.excepciones.ContrasenaActualIncorrecta.class,
                () -> controlador.verificarContrasena(auth, dto)
        );
    }

    @Test
    @DisplayName("POST /api/usuarios/{id}/seguidores KO: lanza IllegalArgumentException si intenta seguirse a si mismo")
    void testSeguirAMiMismo() {
        servicioUsuario.nuevoUsuario(usuario("Narcisista", "hash", "narciso@gmail.com"));
        Long id = servicioUsuario.buscarUsuario("Narcisista").orElseThrow().getId();
        Authentication auth = new UsernamePasswordAuthenticationToken("Narcisista", null);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> controlador.seguir(id, auth)
        );
    }

    private Usuario usuario(String nombre, String contrasena, String email) {
        return new Usuario(null, nombre, contrasena, email, null, null, Tipousuario.USER, false);
    }
}
