package es.plotgram.backend.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    private Usuario usuarioA;
    private Usuario usuarioB;

    @BeforeEach
    void setUp() {
        usuarioA = new Usuario();
        usuarioA.setNombre("UsuarioA");

        usuarioB = new Usuario();
        usuarioB.setNombre("UsuarioB");
    }

    @Test
    @DisplayName("seguir OK: aÃ±ade a 'usuarioB' en seguidos de 'usuarioA' y a 'usuarioA' en seguidores de 'usuarioB'")
    void testSeguirUsuario() {
        usuarioA.seguir(usuarioB);
        assertThat(usuarioA.getSeguidos()).containsExactly(usuarioB);
        assertThat(usuarioB.getSeguidores()).containsExactly(usuarioA);
    }

    @Test
    @DisplayName("seguir OK: no duplica si ya lo sigue")
    void testSeguirUsuarioDuplicado() {
        usuarioA.seguir(usuarioB);
        usuarioA.seguir(usuarioB);
        assertThat(usuarioA.getSeguidos()).hasSize(1);
        assertThat(usuarioB.getSeguidores()).hasSize(1);
    }

    @Test
    @DisplayName("dejarDeSeguir OK: elimina la relacion en ambas direcciones")
    void testDejarDeSeguir() {
        usuarioA.seguir(usuarioB);
        assertThat(usuarioA.getSeguidos()).contains(usuarioB);
        usuarioA.dejarDeSeguir(usuarioB);
        assertThat(usuarioA.getSeguidos()).isEmpty();
        assertThat(usuarioB.getSeguidores()).isEmpty();
    }

    @Test
    @DisplayName("dejarDeSeguir OK: no hace nada si no lo seguÃ­a")
    void testDejarDeSeguirSinSeguirPreviamente() {
        Usuario usuarioC = new Usuario();
        usuarioC.setNombre("UsuarioC");
        usuarioA.dejarDeSeguir(usuarioC);
        assertThat(usuarioA.getSeguidos()).isEmpty();
        assertThat(usuarioC.getSeguidores()).isEmpty();
    }
}
