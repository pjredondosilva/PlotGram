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

/**
 * Servicio encargado de gestionar los perfiles de usuario y su persistencia.
 * Proporciona métodos para registrar, buscar, actualizar y verificar usuarios.
 */
@Service
@Validated
public class ServicioUsuario {
    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    public ServicioUsuario(RepositorioUsuario repositorioUsuario, 
                           PasswordEncoder passwordEncoder) {
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Verifica que el nombre y el email no estén ya registrados.
     * 
     * @param usuario Datos del nuevo usuario.
     */
    public void nuevoUsuario(Usuario usuario) {
        if (usuario.getNombre() != null && repositorioUsuario.buscarPorNombre(usuario.getNombre()).isPresent()) {
            throw new UsuarioYaRegistrado("nombre");
        }
        if (usuario.getEmail() != null && repositorioUsuario.buscarPorEmail(usuario.getEmail()).isPresent()) {
            throw new UsuarioYaRegistrado("email");
        }
        repositorioUsuario.guardar(usuario);
    }

    /**
     * Busca un usuario por su ID interno.
     * 
     * @param id ID del usuario.
     * @return Optional con el usuario si existe.
     */
    public Optional<Usuario> buscarUsuario(Long id) {
        return repositorioUsuario.buscarPorID(id);
    }

    /**
     * Busca un usuario por su nombre de usuario.
     * 
     * @param nombre Nombre de usuario.
     * @return Optional con el usuario si existe.
     */
    public Optional<Usuario> buscarUsuario(String nombre) {
        return repositorioUsuario.buscarPorNombre(nombre);
    }

    /**
     * Verifica si la contraseña proporcionada coincide con la actual del usuario.
     * 
     * @param nombreUsuario Nombre del usuario.
     * @param contrasenaActual Contraseña a verificar.
     */
    public void verificarContrasenaActual(String nombreUsuario, String contrasenaActual) {
        Usuario usuario = obtenerUsuarioActivo(nombreUsuario);
        validarContrasenaActual(usuario, contrasenaActual);
    }

    /**
     * Actualiza la información del perfil de un usuario.
     * Realiza validaciones de seguridad y unicidad de datos.
     * 
     * @return El usuario actualizado.
     */
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

        return repositorioUsuario.actualizar(usuario);
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

    /**
     * Busca usuarios que coincidan con un término de búsqueda.
     */
    public java.util.List<Usuario> buscarUsuarios(String query) {
        if (query == null || query.isBlank()) return java.util.Collections.emptyList();
        return repositorioUsuario.buscarPorNombreCoincidencia(query.trim(), 10);
    }

    /**
     * Un usuario sigue a otro.
     */
    @Transactional
    public void seguir(Long idSeguido, String nombreSeguidor) {
        Usuario seguidor = obtenerUsuarioActivo(nombreSeguidor);
        Usuario seguido = repositorioUsuario.buscarPorID(idSeguido)
                .orElseThrow(UsuarioNoEncontrado::new);

        if (seguidor.getId().equals(seguido.getId())) {
            throw new IllegalArgumentException("No puedes seguirte a ti mismo");
        }

        seguidor.seguir(seguido);
        repositorioUsuario.actualizar(seguidor);
    }

    /**
     * Un usuario deja de seguir a otro.
     */
    @Transactional
    public void dejarDeSeguir(Long idSeguido, String nombreSeguidor) {
        Usuario seguidor = obtenerUsuarioActivo(nombreSeguidor);
        Usuario seguido = repositorioUsuario.buscarPorID(idSeguido)
                .orElseThrow(UsuarioNoEncontrado::new);

        seguidor.dejarDeSeguir(seguido);
        repositorioUsuario.actualizar(seguidor);
    }

    /**
     * Comprueba si un usuario sigue a otro.
     */
    @Transactional(readOnly = true)
    public boolean esSeguidor(String nombreSeguidor, Long idSeguido) {
        Usuario seguidor = obtenerUsuarioActivo(nombreSeguidor);
        return seguidor.getSeguidos().stream()
                .anyMatch(u -> u.getId().equals(idSeguido));
    }
}
