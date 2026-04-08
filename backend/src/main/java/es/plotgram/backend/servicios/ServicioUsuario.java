package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.excepciones.ContrasenaActualIncorrecta;
import es.plotgram.backend.excepciones.UsuarioNoEncontrado;
import es.plotgram.backend.excepciones.UsuarioYaRegistrado;
import es.plotgram.backend.repositorios.RepositorioUsuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class ServicioUsuario {
    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    public ServicioUsuario(RepositorioUsuario repositorioUsuario, PasswordEncoder passwordEncoder) {
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    public void nuevoUsuario(Usuario usuario) {
        if (usuario.getNombre() != null && repositorioUsuario.buscarPorNombre(usuario.getNombre()).isPresent()) {
            throw new UsuarioYaRegistrado("nombre");
        }
        if (usuario.getEmail() != null && repositorioUsuario.buscarPorEmail(usuario.getEmail()).isPresent()) {
            throw new UsuarioYaRegistrado("email");
        }
        repositorioUsuario.guardar(usuario);
    }

    public Optional<Usuario> buscarUsuario(long id) {
        return repositorioUsuario.buscarPorID(id);
    }

    public Optional<Usuario> buscarUsuario(String nombre) {
        return repositorioUsuario.buscarPorNombre(nombre);
    }

    @Transactional(readOnly = true)
    public void verificarContrasenaActual(String nombreUsuario, String contrasenaActual) {
        Usuario usuario = obtenerUsuarioActivo(nombreUsuario);
        validarContrasenaActual(usuario, contrasenaActual);
    }

    @Transactional
    public Usuario actualizarPerfil(String nombreUsuario,
                                    String nuevoNombre,
                                    String nuevoEmail,
                                    String fotoPerfil,
                                    String descripcion,
                                    String contrasenaActual,
                                    String nuevaContrasena) {
        Usuario usuario = obtenerUsuarioActivo(nombreUsuario);

        validarContrasenaActual(usuario, contrasenaActual);
        validarUnicidad(nuevoNombre, nuevoEmail, usuario.getId());

        usuario.setNombre(nuevoNombre.trim());
        usuario.setEmail(nuevoEmail.trim());
        usuario.setFotoPerfil(normalizarOpcional(fotoPerfil));
        usuario.setDescripcion(normalizarOpcional(descripcion));

        String contrasenaNormalizada = normalizarOpcional(nuevaContrasena);
        if (contrasenaNormalizada != null) {
            usuario.setContrasena(passwordEncoder.encode(contrasenaNormalizada));
        }

        return repositorioUsuario.guardar(usuario);
    }

    private Usuario obtenerUsuarioActivo(String nombreUsuario) {
        return repositorioUsuario.buscarPorNombre(nombreUsuario)
                .orElseThrow(UsuarioNoEncontrado::new);
    }

    private void validarContrasenaActual(Usuario usuario, String contrasenaActual) {
        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
            throw new ContrasenaActualIncorrecta();
        }
    }

    private void validarUnicidad(String nombre, String email, Long idUsuarioActual) {
        repositorioUsuario.buscarPorNombre(nombre.trim())
                .filter(otro -> !otro.getId().equals(idUsuarioActual))
                .ifPresent(otro -> {
                    throw new UsuarioYaRegistrado("nombre");
                });

        repositorioUsuario.buscarPorEmail(email.trim())
                .filter(otro -> !otro.getId().equals(idUsuarioActual))
                .ifPresent(otro -> {
                    throw new UsuarioYaRegistrado("email");
                });
    }

    private String normalizarOpcional(String valor) {
        if (valor == null) return null;
        String limpio = valor.trim();
        return limpio.isBlank() ? null : limpio;
    }
}
