package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.excepciones.UsuarioYaRegistrado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = es.plotgram.backend.app.BackendApplication.class)
@ActiveProfiles("test")
public class ServicioUsuarioTest {
    @Autowired
    private ServicioUsuario servicio;

    @Test
    @DisplayName("NuevoUsuario OK: guarda el usuario si no existe ni nombre ni email")
    void testNuevoUsuarioValido() {
        var usuario = new Usuario(0L,"Manolo", "1234", "manolo@gmail.com", Tipousuario.USER,false);
        servicio.nuevoUsuario(usuario);
        assertThat(servicio.buscarUsuario("Manolo")).isPresent();
    }

    @Test
    @DisplayName("NuevoUsuario KO: no permite registrar un usuario si el nombre ya existe")
    void testNuevoUsuarioNombreRepetido() {
        var usuario1 = new Usuario(0L,"Pedro", "1234", "pedro@gmail.com", Tipousuario.USER,false);
        var usuario2 =new Usuario(0L,"Pedro", "1234", "otro@gmail.com", Tipousuario.USER,false);
        servicio.nuevoUsuario(usuario1);
        assertThatThrownBy(() -> servicio.nuevoUsuario(usuario2))
                .isInstanceOf(UsuarioYaRegistrado.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("NuevoUsuario KO: no permite registrar un usuario si el nombre ya existe")
    void testNuevoUsuarioEmailRepetido() {
        var usuario1 = new Usuario(0L,"Pepe", "1234", "pepe@gmail.com", Tipousuario.USER,false);
        var usuario2 =new Usuario(0L,"Antonio", "1234", "antonio@gmail.com", Tipousuario.USER,false);
        servicio.nuevoUsuario(usuario1);
        assertThatThrownBy(() -> servicio.nuevoUsuario(usuario2))
                .isInstanceOf(UsuarioYaRegistrado.class)
                .hasMessageContaining("email");
    }
}
