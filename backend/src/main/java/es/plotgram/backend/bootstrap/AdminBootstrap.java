package es.plotgram.backend.bootstrap;

import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.repositorios.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final RepositorioUsuario repo;
    private final PasswordEncoder encoder;

    @Value("${plotgram.admin.nombre:admin}")
    private String adminNombre;

    @Value("${plotgram.admin.password:admin123}")
    private String adminPassword;

    public AdminBootstrap(RepositorioUsuario repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (repo.buscarPorNombre(adminNombre).isPresent()) return;

        Usuario admin = new Usuario();
        admin.setNombre(adminNombre);
        admin.setContrasena(encoder.encode(adminPassword));
        admin.setTipo(Tipousuario.ADMIN);
        admin.setBorrado(false);

        repo.guardar(admin);

        System.out.println("[BOOTSTRAP] Admin creado: " + adminNombre);
    }
}
