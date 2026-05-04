package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.Valoracion;
import es.plotgram.backend.entidades.Contenido;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class RepositorioValoracion {

    @PersistenceContext
    EntityManager em;

    @Transactional
    public Valoracion save(Valoracion valoracion) {
        if (valoracion.getId() == null) {
            em.persist(valoracion);
            return valoracion;
        } else {
            return em.merge(valoracion);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Valoracion> buscarPorTmdbIdYTipo(Long tmdbId, String tipo) {
        var q = em.createQuery("""
                SELECT v 
                FROM Valoracion v 
                WHERE v.contenido.tmdbId = :tmdbId 
                  AND v.contenido.tipo = :tipo 
                ORDER BY v.fecha DESC
                """, Valoracion.class);
        q.setParameter("tmdbId", tmdbId);
        q.setParameter("tipo", tipo);
        return q.getResultList();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Double calcularMediaPorTmdbIdYTipo(Long tmdbId, String tipo) {
        var q = em.createQuery("""
                SELECT AVG(CAST(v.puntuacion AS double)) 
                FROM Valoracion v 
                WHERE v.contenido.tmdbId = :tmdbId 
                  AND v.contenido.tipo = :tipo
                """, Double.class);
        q.setParameter("tmdbId", tmdbId);
        q.setParameter("tipo", tipo);
        return q.getSingleResult();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public long contarPorTmdbIdYTipo(Long tmdbId, String tipo) {
        var q = em.createQuery("""
                SELECT COUNT(v) 
                FROM Valoracion v 
                WHERE v.contenido.tmdbId = :tmdbId 
                  AND v.contenido.tipo = :tipo
                """, Long.class);
        q.setParameter("tmdbId", tmdbId);
        q.setParameter("tipo", tipo);
        return q.getSingleResult();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Valoracion> buscarPorUsuarioYContenido(Long usuarioId, Long tmdbId, String tipo) {
        var q = em.createQuery("""
                SELECT v 
                FROM Valoracion v 
                WHERE v.usuario.id = :usuarioId 
                  AND v.contenido.tmdbId = :tmdbId 
                  AND v.contenido.tipo = :tipo
                """, Valoracion.class);
        q.setParameter("usuarioId", usuarioId);
        q.setParameter("tmdbId", tmdbId);
        q.setParameter("tipo", tipo);
        q.setMaxResults(1);
        return q.getResultList().stream().findFirst();
    }
}
