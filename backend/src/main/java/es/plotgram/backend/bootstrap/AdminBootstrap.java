package es.plotgram.backend.bootstrap;

import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.repositorios.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializa los datos básicos de arranque de la aplicación.
 * En concreto, crea un usuario administrador si no existe ya uno con el
 * nombre configurado en las propiedades de la aplicación.
 */
@Component
public class AdminBootstrap implements CommandLineRunner {

    private final RepositorioUsuario repo;
    private final PasswordEncoder encoder;

    @Value("${plotgram.admin.nombre:admin}")
    private String adminNombre;

    @Value("${plotgram.admin.password:admin123}")
    private String adminPassword;

    @Value("${plotgram.admin.email:admin@email.com}")
    private String adminEmail;

    public AdminBootstrap(RepositorioUsuario repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }


    /**
     * Comprueba si existe el administrador configurado y, en caso contrario,
     * lo crea con los valores definidos en la configuración.
     *
     * @param args argumentos de arranque de la aplicación
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (repo.buscarPorNombre(adminNombre).isPresent()) return;

        Usuario admin = new Usuario();
        admin.setNombre(adminNombre);
        admin.setContrasena(encoder.encode(adminPassword));
        admin.setEmail(adminEmail);
        admin.setTipo(Tipousuario.ADMIN);
        admin.setBorrado(false);

        repo.guardar(admin);

        System.out.println("[BOOTSTRAP] Admin creado: " + adminNombre);
    }
}
