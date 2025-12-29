package es.plotgram.backend.seguridad.Credenciales;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.servicios.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ServicioCredencialesUsuario implements UserDetailsService {

    @Autowired
    ServicioUsuario serviciousuario;

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
