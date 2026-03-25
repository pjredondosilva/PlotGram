package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.excepciones.ContrasenaActualIncorrecta;
import es.plotgram.backend.excepciones.UsuarioNoEncontrado;
import es.plotgram.backend.excepciones.UsuarioYaRegistrado;
import es.plotgram.backend.repositorios.RepositorioUsuario;
import es.plotgram.backend.rest.dto.DActualizacionPerfil;
import es.plotgram.backend.rest.dto.DVerificacionContrasena;
import jakarta.validation.Valid;
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

    public void nuevoUsuario(@Valid Usuario usuario) {
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
    public void verificarContrasenaActual(String nombreUsuario, @Valid DVerificacionContrasena dto) {
        Usuario usuario = obtenerUsuarioActivo(nombreUsuario);
        validarContrasenaActual(usuario, dto.contrasenaActual());
    }

    @Transactional
    public Usuario actualizarPerfil(String nombreUsuario, @Valid DActualizacionPerfil dto) {
        Usuario usuario = obtenerUsuarioActivo(nombreUsuario);

        if (!passwordEncoder.matches(dto.contrasenaActual(), usuario.getContrasena())) {
            throw new ContrasenaActualIncorrecta();
        }

        String nuevoNombre = dto.nombre().trim();
        String nuevoEmail = dto.email().trim();

        repositorioUsuario.buscarPorNombre(nuevoNombre)
                .filter(u -> !u.getId().equals(usuario.getId()))
                .ifPresent(u -> {
                    throw new UsuarioYaRegistrado("nombre");
                });

        repositorioUsuario.buscarPorEmail(nuevoEmail)
                .filter(u -> !u.getId().equals(usuario.getId()))
                .ifPresent(u -> {
                    throw new UsuarioYaRegistrado("email");
                });

        usuario.setNombre(nuevoNombre);
        usuario.setEmail(nuevoEmail);
        usuario.setFotoPerfil(normalizarOpcional(dto.fotoPerfil()));
        usuario.setDescripcion(normalizarOpcional(dto.descripcion()));

        String nuevaContrasena = normalizarOpcional(dto.nuevaContrasena());
        if (nuevaContrasena != null) {
            usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
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

    private void validarUnicidad(DActualizacionPerfil dto, Long idUsuarioActual) {
        repositorioUsuario.buscarPorNombre(dto.nombre().trim())
                .filter(otro -> !otro.getId().equals(idUsuarioActual))
                .ifPresent(otro -> {
                    throw new UsuarioYaRegistrado("nombre");
                });

        repositorioUsuario.buscarPorEmail(dto.email().trim())
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