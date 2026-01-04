package es.plotgram.backend.rest.dto;
import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.repositorios.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class Mapeador {
    @Autowired
    RepositorioUsuario repositorioUsuarios;
    @Autowired
    PasswordEncoder codificadorClaves;

    public Dusuario dto(Usuario usuario) {
        return new Dusuario(
                usuario.getId(),
                usuario.getNombre(),
                null,
                usuario.getEmail(),
                usuario.getTipo(),
                usuario.isBorrado()
        );
    }
    public Usuario entidad(Dusuario dUsuario) {
        return new Usuario(
                dUsuario.id(),
                dUsuario.nombre(),
                dUsuario.contrasenia(),
                dUsuario.email(),
                dUsuario.tipo(),
                dUsuario.borrado()
        );
    }

    public Usuario entidadNueva(DUsuarioRegistro d) {
        Usuario u = new Usuario();
        u.setNombre(d.nombre());
        u.setContrasena(codificadorClaves.encode(d.contrasenia()));
        u.setEmail(d.email());
        u.setTipo(Tipousuario.USER);
        u.setBorrado(false);
        return u;
    }


}