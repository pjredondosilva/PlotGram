package es.plotgram.backend.seguridad.Credenciales;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.servicios.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio de seguridad encargado de cargar los detalles de credenciales y roles
 * de un usuario a partir de su nombre de usuario, para la autenticación en Spring Security.
 */
@Service
public class ServicioCredencialesUsuario implements UserDetailsService {

    @Autowired
    ServicioUsuario serviciousuario;

    /**
     * Recupera el usuario por su nombre y construye el objeto UserDetails
     * que define sus credenciales y roles (ADMIN, USUARIO).
     *
     * @param nombre Nombre del usuario a buscar.
     * @return UserDetails con el nombre, contraseña codificada y roles del usuario.
     * @throws UsernameNotFoundException Si el usuario no existe en la base de datos.
     */
    @Override
    public UserDetails loadUserByUsername(String nombre) throws UsernameNotFoundException {
        Usuario usuario = serviciousuario.buscarUsuario(nombre).orElseThrow(() -> new UsernameNotFoundException("No existe el usuario: " + nombre));

        var builder = org.springframework.security.core.userdetails.User.withUsername(usuario.getNombre())
                .password(usuario.getContrasena());

        if (usuario.getTipo() == Tipousuario.ADMIN) {
            builder.roles("ADMIN", "USUARIO");
        } else {
            builder.roles("USUARIO");
        }
        return builder.build();
    }

}
