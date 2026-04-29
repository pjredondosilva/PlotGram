package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.excepciones.ContrasenaActualIncorrecta;
import es.plotgram.backend.excepciones.UsuarioNoEncontrado;
import es.plotgram.backend.excepciones.UsuarioYaRegistrado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

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
class ServicioUsuarioTest {

    @Autowired
    private ServicioUsuario servicio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("nuevoUsuario OK: guarda el usuario si no existe ni nombre ni email")
    void testNuevoUsuarioValido() {
        Usuario usuario = usuario("AlbaRegistro", "hash-alba", "alba.registro@gmail.com");

        servicio.nuevoUsuario(usuario);

        assertThat(servicio.buscarUsuario("AlbaRegistro")).isPresent();
        assertThat(servicio.buscarUsuario("AlbaRegistro").orElseThrow().getEmail())
                .isEqualTo("alba.registro@gmail.com");
    }

    @Test
    @DisplayName("nuevoUsuario KO: no permite registrar un usuario si el nombre ya existe")
    void testNuevoUsuarioNombreRepetido() {
        Usuario usuario1 = usuario("BrunoRegistro", "hash-bruno", "bruno.registro@gmail.com");
        Usuario usuario2 = usuario("BrunoRegistro", "hash-otro", "otro.bruno@gmail.com");

        servicio.nuevoUsuario(usuario1);

        assertThatThrownBy(() -> servicio.nuevoUsuario(usuario2))
                .isInstanceOf(UsuarioYaRegistrado.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("nuevoUsuario KO: no permite registrar un usuario si el email ya existe")
    void testNuevoUsuarioEmailRepetido() {
        Usuario usuario1 = usuario("ClaraRegistro", "hash-clara", "clara.registro@gmail.com");
        Usuario usuario2 = usuario("CarlosRegistro", "hash-carlos", "clara.registro@gmail.com");

        servicio.nuevoUsuario(usuario1);

        assertThatThrownBy(() -> servicio.nuevoUsuario(usuario2))
                .isInstanceOf(UsuarioYaRegistrado.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("buscarUsuario por id OK: devuelve el usuario cuando existe")
    void testBuscarUsuarioPorIdExistente() {
        servicio.nuevoUsuario(usuario("DanielBusqueda", "hash-daniel", "daniel.busqueda@gmail.com"));
        Long id = servicio.buscarUsuario("DanielBusqueda").orElseThrow().getId();

        var resultado = servicio.buscarUsuario(id);

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getNombre()).isEqualTo("DanielBusqueda");
    }

    @Test
    @DisplayName("buscarUsuario por nombre OK: devuelve el usuario cuando existe")
    void testBuscarUsuarioPorNombreExistente() {
        servicio.nuevoUsuario(usuario("ElenaBusqueda", "hash-elena", "elena.busqueda@gmail.com"));

        var resultado = servicio.buscarUsuario("ElenaBusqueda");

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getEmail()).isEqualTo("elena.busqueda@gmail.com");
    }

    @Test
    @DisplayName("verificarContrasenaActual OK: no lanza excepción si la contraseña coincide")
    void testVerificarContrasenaActualValida() {
        servicio.nuevoUsuario(usuario("FabioPassword", passwordEncoder.encode("ClaveFabio1!"), "fabio.password@gmail.com"));

        assertThatCode(() -> servicio.verificarContrasenaActual("FabioPassword", "ClaveFabio1!"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("verificarContrasenaActual KO: lanza excepción si el usuario no existe")
    void testVerificarContrasenaActualUsuarioNoExiste() {
        assertThatThrownBy(() -> servicio.verificarContrasenaActual("GemaPassword", "ClaveGema1!"))
                .isInstanceOf(UsuarioNoEncontrado.class);
    }

    @Test
    @DisplayName("verificarContrasenaActual KO: lanza excepción si la contraseña no coincide")
    void testVerificarContrasenaActualIncorrecta() {
        servicio.nuevoUsuario(usuario("HugoPassword", passwordEncoder.encode("ClaveHugo1!"), "hugo.password@gmail.com"));

        assertThatThrownBy(() -> servicio.verificarContrasenaActual("HugoPassword", "ClaveMala1!"))
                .isInstanceOf(ContrasenaActualIncorrecta.class);
    }

    @Test
    @DisplayName("actualizarPerfil OK: actualiza datos, recorta campos y mantiene contraseña si no se informa una nueva")
    void testActualizarPerfilSinNuevaContrasena() {
        servicio.nuevoUsuario(usuario("IreneActual", passwordEncoder.encode("ClaveIrene1!"), "irene.actual@gmail.com"));
        String hashOriginal = servicio.buscarUsuario("IreneActual").orElseThrow().getContrasena();

        Usuario resultado = servicio.actualizarPerfil(
                "IreneActual",
                "  IreneNueva  ",
                "  irene.nueva@gmail.com  ",
                "   ",
                "  Nueva descripción de Irene  ",
                "ClaveIrene1!",
                "   "
        );

        assertThat(resultado.getNombre()).isEqualTo("IreneNueva");
        assertThat(resultado.getEmail()).isEqualTo("irene.nueva@gmail.com");
        assertThat(resultado.getFotoPerfil()).isNull();
        assertThat(resultado.getDescripcion()).isEqualTo("Nueva descripción de Irene");
        assertThat(resultado.getContrasena()).isEqualTo(hashOriginal);
    }

    @Test
    @DisplayName("actualizarPerfil OK: codifica la nueva contraseña cuando se informa")
    void testActualizarPerfilConNuevaContrasena() {
        servicio.nuevoUsuario(usuario("JuliaActual", passwordEncoder.encode("ClaveJulia1!"), "julia.actual@gmail.com"));
        String hashOriginal = servicio.buscarUsuario("JuliaActual").orElseThrow().getContrasena();

        Usuario resultado = servicio.actualizarPerfil(
                "JuliaActual",
                "JuliaNueva",
                "julia.nueva@gmail.com",
                "https://imagenes.test/julia.jpg",
                "Perfil de Julia",
                "ClaveJulia1!",
                "NuevaJulia1!"
        );

        assertThat(resultado.getNombre()).isEqualTo("JuliaNueva");
        assertThat(resultado.getEmail()).isEqualTo("julia.nueva@gmail.com");
        assertThat(resultado.getFotoPerfil()).isEqualTo("https://imagenes.test/julia.jpg");
        assertThat(resultado.getDescripcion()).isEqualTo("Perfil de Julia");
        assertThat(resultado.getContrasena()).isNotEqualTo(hashOriginal);
        assertThat(passwordEncoder.matches("NuevaJulia1!", resultado.getContrasena())).isTrue();
    }

    @Test
    @DisplayName("actualizarPerfil KO: no permite usar el nombre de otro usuario")
    void testActualizarPerfilNombreRepetido() {
        servicio.nuevoUsuario(usuario("KikoActual", passwordEncoder.encode("ClaveKiko1!"), "kiko.actual@gmail.com"));
        servicio.nuevoUsuario(usuario("KikoOcupado", passwordEncoder.encode("ClaveKiko2!"), "kiko.ocupado@gmail.com"));

        assertThatThrownBy(() -> servicio.actualizarPerfil(
                "KikoActual",
                "KikoOcupado",
                "kiko.nuevo@gmail.com",
                null,
                null,
                "ClaveKiko1!",
                null
        ))
                .isInstanceOf(UsuarioYaRegistrado.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("actualizarPerfil KO: no permite usar el email de otro usuario")
    void testActualizarPerfilEmailRepetido() {
        servicio.nuevoUsuario(usuario("LauraActual", passwordEncoder.encode("ClaveLaura1!"), "laura.actual@gmail.com"));
        servicio.nuevoUsuario(usuario("LauraOcupada", passwordEncoder.encode("ClaveLaura2!"), "laura.ocupada@gmail.com"));

        assertThatThrownBy(() -> servicio.actualizarPerfil(
                "LauraActual",
                "LauraNueva",
                "laura.ocupada@gmail.com",
                null,
                null,
                "ClaveLaura1!",
                null
        ))
                .isInstanceOf(UsuarioYaRegistrado.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("actualizarPerfil OK: permite mantener el mismo nombre y email del usuario actual")
    void testActualizarPerfilManteniendoNombreYEmailPropios() {
        servicio.nuevoUsuario(usuario("MarcosActual", passwordEncoder.encode("ClaveMarcos1!"), "marcos.actual@gmail.com"));

        Usuario resultado = servicio.actualizarPerfil(
                "MarcosActual",
                "MarcosActual",
                "marcos.actual@gmail.com",
                null,
                "Descripción Marcos",
                "ClaveMarcos1!",
                null
        );

        assertThat(resultado.getNombre()).isEqualTo("MarcosActual");
        assertThat(resultado.getEmail()).isEqualTo("marcos.actual@gmail.com");
        assertThat(resultado.getDescripcion()).isEqualTo("Descripción Marcos");
    }

    private Usuario usuario(String nombre, String contrasena, String email) {
        return new Usuario(null, nombre, contrasena, email, null, null, Tipousuario.USER, false);
    }
}
