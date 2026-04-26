package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.Lista;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class RepositorioLista {

    @PersistenceContext
    EntityManager em;

    @Transactional
    public Lista guardar(Lista lista) {
        em.persist(lista);
        return lista;
    }

    @Transactional
    public Lista actualizar(Lista lista) {
        return em.merge(lista);
    }
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Lista> buscarPorUsuarioId(Long usuarioId) {
        var q = em.createQuery("""
                SELECT l
                FROM Lista l
                WHERE l.usuario.id = :usuarioId
                ORDER BY l.id DESC
                """, Lista.class);
        q.setParameter("usuarioId", usuarioId);
        return q.getResultList();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Lista> buscarPorIdYUsuarioId(Long idLista, Long usuarioId) {
        var q = em.createQuery("""
                SELECT l
                FROM Lista l
                WHERE l.id = :idLista
                  AND l.usuario.id = :usuarioId
                """, Lista.class);
        q.setParameter("idLista", idLista);
        q.setParameter("usuarioId", usuarioId);
        q.setMaxResults(1);
        return q.getResultList().stream().findFirst();
    }
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean existePorUsuarioIdYNombre(Long usuarioId, String nombre) {
        var q = em.createQuery("""
                SELECT COUNT(l)
                FROM Lista l
                WHERE l.usuario.id = :usuarioId
                  AND LOWER(TRIM(l.nombre)) = :nombreNormalizado
                """, Long.class);

        q.setParameter("usuarioId", usuarioId);
        q.setParameter("nombreNormalizado", normalizarNombreParaBusqueda(nombre));

        return q.getSingleResult() > 0;
    }
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean existePorUsuarioIdYNombreEIdDistinto(Long usuarioId, String nombre, Long idLista) {
        var q = em.createQuery("""
                SELECT COUNT(l)
                FROM Lista l
                WHERE l.usuario.id = :usuarioId
                  AND l.id <> :idLista
                  AND LOWER(TRIM(l.nombre)) = :nombreNormalizado
                """, Long.class);

        q.setParameter("usuarioId", usuarioId);
        q.setParameter("idLista", idLista);
        q.setParameter("nombreNormalizado", normalizarNombreParaBusqueda(nombre));

        return q.getSingleResult() > 0;
    }
    @Transactional
    public void borrar(Lista lista) {
        Lista gestionada = em.contains(lista) ? lista : em.merge(lista);
        em.remove(gestionada);
    }
    private String normalizarNombreParaBusqueda(String nombre) {
        return nombre == null ? "" : nombre.trim().toLowerCase(Locale.ROOT);
    }
}