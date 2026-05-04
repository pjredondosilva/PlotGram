package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.Usuario;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link Usuario}.
 * Proporciona operaciones básicas de persistencia y consulta de usuarios.
 */
@Repository
public class RepositorioUsuario {
@PersistenceContext
EntityManager em;

    /**
     * Guarda un usuario nuevo en la base de datos.
     * @param usuario El usuario a persistir.
     *///
    @Transactional
    public Usuario guardar(Usuario usuario) {
        em.persist(usuario);
        return usuario;
    }

    @Transactional
    public Usuario actualizar(Usuario usuario) {
        return em.merge(usuario);
    }

    /**
     * Busca un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return un {@link Optional} con el usuario encontrado, o vacío si no existe
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Usuario> buscarPorID(long id) {
        return Optional.ofNullable(em.find(Usuario.class, id));
    }

    /**
     * Busca un usuario activo por su nombre.
     * La comparación no distingue entre mayúsculas y minúsculas.
     *
     * @param nombre nombre del usuario a buscar
     * @return un {@link Optional} con el usuario encontrado, o vacío si no existe
     */
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

    /**
     * Busca un usuario activo por su correo electrónico.
     *
     * @param email correo electrónico del usuario a buscar
     * @return un {@link Optional} con el usuario encontrado, o vacío si no existe
     */
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

