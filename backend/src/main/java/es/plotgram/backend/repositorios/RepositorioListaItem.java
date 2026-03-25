package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.ListaItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class RepositorioListaItem {

    @PersistenceContext
    EntityManager em;

    @Transactional
    public ListaItem guardar(ListaItem listaItem) {
        if (listaItem.getId() == null) {
            em.persist(listaItem);
            return listaItem;
        }
        return em.merge(listaItem);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<ListaItem> buscarPorListaIdOrdenados(Long idLista) {
        var q = em.createQuery("""
                SELECT li
                FROM ListaItem li
                JOIN FETCH li.contenido
                WHERE li.lista.id = :idLista
                ORDER BY li.orden ASC, li.id ASC
                """, ListaItem.class);
        q.setParameter("idLista", idLista);
        return q.getResultList();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public long contarPorListaId(Long idLista) {
        var q = em.createQuery("""
                SELECT COUNT(li)
                FROM ListaItem li
                WHERE li.lista.id = :idLista
                """, Long.class);
        q.setParameter("idLista", idLista);
        return q.getSingleResult();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean existePorListaIdYContenidoId(Long idLista, Long idContenido) {
        var q = em.createQuery("""
                SELECT COUNT(li)
                FROM ListaItem li
                WHERE li.lista.id = :idLista
                  AND li.contenido.id = :idContenido
                """, Long.class);
        q.setParameter("idLista", idLista);
        q.setParameter("idContenido", idContenido);
        return q.getSingleResult() > 0;
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Integer buscarUltimoOrdenDeLista(Long idLista) {
        var q = em.createQuery("""
                SELECT MAX(li.orden)
                FROM ListaItem li
                WHERE li.lista.id = :idLista
                """, Integer.class);
        q.setParameter("idLista", idLista);
        return q.getSingleResult();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<ListaItem> buscarPorIdYListaId(Long idItem, Long idLista) {
        var q = em.createQuery("""
                SELECT li
                FROM ListaItem li
                JOIN FETCH li.contenido
                WHERE li.id = :idItem
                  AND li.lista.id = :idLista
                """, ListaItem.class);
        q.setParameter("idItem", idItem);
        q.setParameter("idLista", idLista);
        q.setMaxResults(1);
        return q.getResultList().stream().findFirst();
    }

    @Transactional
    public void borrar(ListaItem listaItem) {
        ListaItem gestionado = em.contains(listaItem) ? listaItem : em.merge(listaItem);
        em.remove(gestionado);
    }
}