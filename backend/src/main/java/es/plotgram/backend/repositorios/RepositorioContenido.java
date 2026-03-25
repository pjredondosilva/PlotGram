package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.Contenido;
import es.plotgram.backend.entidades.TipoContenido;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class RepositorioContenido {

    @PersistenceContext
    EntityManager em;

    @Transactional
    public Contenido guardar(Contenido contenido) {
        if (contenido.getId() == null) {
            em.persist(contenido);
            return contenido;
        }
        return em.merge(contenido);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Contenido> buscarPorTmdbIdYTipo(Long tmdbId, TipoContenido tipo) {
        var q = em.createQuery("""
                SELECT c
                FROM Contenido c
                WHERE c.tmdbId = :tmdbId
                  AND c.tipo = :tipo
                """, Contenido.class);
        q.setParameter("tmdbId", tmdbId);
        q.setParameter("tipo", tipo);
        q.setMaxResults(1);
        return q.getResultList().stream().findFirst();
    }
}