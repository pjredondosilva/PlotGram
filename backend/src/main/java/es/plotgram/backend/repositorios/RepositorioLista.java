package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.Lista;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional
    public void borrar(Lista lista) {
        Lista gestionada = em.contains(lista) ? lista : em.merge(lista);
        em.remove(gestionada);
    }
}