package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.Usuario;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class RepositorioUsuario {
@PersistenceContext
EntityManager em;

    /**
     * Guarda un usuario nuevo en la base de datos.
     * @param usuario El usuario a persistir (debe ser nuevo).
     */
    @Transactional
    public void guardar(Usuario usuario) {
        em.persist(usuario);
    }

    /**
     * Busca un usuario por su ID (que es la clave primaria @Id).
     * @param id El ID del usuario a buscar.
     * @return Un Optional que contiene al usuario si se encuentra, o vacío si no.
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Usuario> buscarPorID(long id) {
        return Optional.ofNullable(em.find(Usuario.class, id));
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Usuario> buscarPorNombre(String nombre) {
        var q = em.createQuery("""
        SELECT u FROM Usuario u
        WHERE lower(u.nombre) = lower(:nombre) AND u.borrado = false
    """, Usuario.class);
        q.setParameter("nombre", nombre);
        q.setMaxResults(1);
        return q.getResultList().stream().findFirst();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        var q = em.createQuery("""
        SELECT u FROM Usuario u
        WHERE u.email = :email AND u.borrado = false
    """, Usuario.class);
        q.setParameter("email", email);
        q.setMaxResults(1);
        return q.getResultList().stream().findFirst();
    }


}

