package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.excepciones.UsuarioYaRegistrado;
import es.plotgram.backend.repositorios.RepositorioUsuario;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class ServicioUsuario {
    private final RepositorioUsuario repositorioUsuario;

    /**
     * Constructor del servicio. Inicializa la coleccion interna de usuarios.
     */
    public ServicioUsuario(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }
    /**
     * Registra un nuevo usuario en el sistema, verificando que no esté ya registrado.
     * No se permite registrar un usuario con el mismo ID que el administrador.
     * Si el ID ya existe en el sistema, se lanza una excepción {@link UsuarioYaRegistrado}.
     * @param usuario Usuario a registrar (no nulo y válido).
     * @throws UsuarioYaRegistrado si el usuario ya está registrado o intenta usarse el ID del administrador.
     */
    public void nuevoUsuario(@Valid Usuario usuario) {
        if (repositorioUsuario.buscarPorID(usuario.getId()).isPresent()) {
            throw new UsuarioYaRegistrado();
        }
        repositorioUsuario.guardar(usuario);
    }

    /**
     * Devolver el usuario asociado al DNI dado
     * @param id el DNI del usuario
     * @return un optional con el usuario asociado al DNI
     */
    public Optional<Usuario> buscarUsuario(long id) {
        return repositorioUsuario.buscarPorID(id);
    }

    /**
     * Devolver el usuario asociado al nombre dado
     * @param nombre el nickname del usuario
     * @return un optional con el usuario asociado al nombre
     */
    public Optional<Usuario> buscarUsuario(String nombre) {
        return repositorioUsuario.buscarPorNombre(nombre);
    }
}
