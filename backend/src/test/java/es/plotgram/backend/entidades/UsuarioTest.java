package es.plotgram.backend.entidades;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    @Test
    @DisplayName("seguir OK: añade al usuario destino en la lista de seguidos del origen y viceversa")
    void testSeguirUsuario() {
        Usuario usuarioOrigen = new Usuario();
        usuarioOrigen.setNombre("Juan");

        Usuario usuarioDestino = new Usuario();
        usuarioDestino.setNombre("Maria");

        usuarioOrigen.seguir(usuarioDestino);

        assertThat(usuarioOrigen.getSeguidos()).containsExactly(usuarioDestino);
        assertThat(usuarioDestino.getSeguidores()).containsExactly(usuarioOrigen);
    }

    @Test
    @DisplayName("seguir OK: no duplica la relación si el usuario origen ya sigue al destino")
    void testSeguirUsuarioDuplicado() {
        Usuario seguidor = new Usuario();
        seguidor.setNombre("Carlos");

        Usuario seguido = new Usuario();
        seguido.setNombre("Ana");

        seguidor.seguir(seguido);
        seguidor.seguir(seguido);

        assertThat(seguidor.getSeguidos()).hasSize(1);
        assertThat(seguido.getSeguidores()).hasSize(1);
    }

    @Test
    @DisplayName("dejarDeSeguir OK: elimina la relación en ambas direcciones correctamente")
    void testDejarDeSeguir() {
        Usuario usuarioActivo = new Usuario();
        usuarioActivo.setNombre("Luis");

        Usuario usuarioPasivo = new Usuario();
        usuarioPasivo.setNombre("Elena");

        usuarioActivo.seguir(usuarioPasivo);
        assertThat(usuarioActivo.getSeguidos()).contains(usuarioPasivo);

        usuarioActivo.dejarDeSeguir(usuarioPasivo);
        
        assertThat(usuarioActivo.getSeguidos()).isEmpty();
        assertThat(usuarioPasivo.getSeguidores()).isEmpty();
    }

    @Test
    @DisplayName("dejarDeSeguir OK: no altera el estado si no existía relación de seguimiento previa")
    void testDejarDeSeguirSinSeguirPreviamente() {
        Usuario usuarioIndependiente = new Usuario();
        usuarioIndependiente.setNombre("Pedro");

        Usuario usuarioNoRelacionado = new Usuario();
        usuarioNoRelacionado.setNombre("Marta");

        usuarioIndependiente.dejarDeSeguir(usuarioNoRelacionado);

        assertThat(usuarioIndependiente.getSeguidos()).isEmpty();
        assertThat(usuarioNoRelacionado.getSeguidores()).isEmpty();
    }
}
