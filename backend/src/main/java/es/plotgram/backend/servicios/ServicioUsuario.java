package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.excepciones.UsuarioYaRegistrado;
import es.plotgram.backend.repositorios.RepositorioUsuario;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * Servicio de negocio para el alta y la consulta de usuarios.
 */
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
     * Registra un nuevo usuario comprobando que no exista ya otro con el mismo
     * nombre o correo electrónico.
     *
     * @param usuario usuario a registrar
     * @throws UsuarioYaRegistrado si el nombre o el correo ya están en uso
     */
    public void nuevoUsuario(@Valid Usuario usuario) {
        if (usuario.getNombre() != null && repositorioUsuario.buscarPorNombre(usuario.getNombre()).isPresent()){
            throw new UsuarioYaRegistrado("nombre");
        }
        if (usuario.getEmail() != null && repositorioUsuario.buscarPorEmail(usuario.getEmail()).isPresent()){
            throw new UsuarioYaRegistrado("email");
        }
        repositorioUsuario.guardar(usuario);
    }

    /**
     * Busca un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return un {@link Optional} con el usuario encontrado, o vacío si no existe
     */
    public Optional<Usuario> buscarUsuario(long id) {
        return repositorioUsuario.buscarPorID(id);
    }

    /**
     * Busca un usuario por su nombre.
     *
     * @param nombre nombre del usuario
     * @return un {@link Optional} con el usuario encontrado, o vacío si no existe
     */
    public Optional<Usuario> buscarUsuario(String nombre) {
        return repositorioUsuario.buscarPorNombre(nombre);
    }
}
