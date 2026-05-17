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
    public Valoracion guardar(Valoracion valoracion) {
        em.persist(valoracion);
        return valoracion;
    }

    @Transactional
    public Valoracion actualizar(Valoracion valoracion) {
        return em.merge(valoracion);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Valoracion> buscarPorTmdbIdYTipo(Long tmdbId, String tipo) {
        var q = em.createQuery("""
                SELECT v 
                FROM Valoracion v 
                WHERE v.contenido.tmdbId = :tmdbId 
                  AND TYPE(v.contenido) = :clase 
                ORDER BY v.fecha DESC
                """, Valoracion.class);
        q.setParameter("tmdbId", tmdbId);
        q.setParameter("clase", resolverClase(tipo));
        return q.getResultList();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Double calcularMediaPorTmdbIdYTipo(Long tmdbId, String tipo) {
        var q = em.createQuery("""
                SELECT AVG(CAST(v.puntuacion AS double)) 
                FROM Valoracion v 
                WHERE v.contenido.tmdbId = :tmdbId 
                  AND TYPE(v.contenido) = :clase
                """, Double.class);
        q.setParameter("tmdbId", tmdbId);
        q.setParameter("clase", resolverClase(tipo));
        return q.getSingleResult();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public long contarPorTmdbIdYTipo(Long tmdbId, String tipo) {
        var q = em.createQuery("""
                SELECT COUNT(v) 
                FROM Valoracion v 
                WHERE v.contenido.tmdbId = :tmdbId 
                  AND TYPE(v.contenido) = :clase
                """, Long.class);
        q.setParameter("tmdbId", tmdbId);
        q.setParameter("clase", resolverClase(tipo));
        return q.getSingleResult();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<Valoracion> buscarPorUsuarioYContenido(Long usuarioId, Long tmdbId, String tipo) {
        var q = em.createQuery("""
                SELECT v 
                FROM Valoracion v 
                WHERE v.usuario.id = :usuarioId 
                  AND v.contenido.tmdbId = :tmdbId 
                  AND TYPE(v.contenido) = :clase
                """, Valoracion.class);
        q.setParameter("usuarioId", usuarioId);
        q.setParameter("tmdbId", tmdbId);
        q.setParameter("clase", resolverClase(tipo));
        q.setMaxResults(1);
        return q.getResultList().stream().findFirst();
    }

    private Class<? extends Contenido> resolverClase(String tipoStr) {
        es.plotgram.backend.entidades.TipoContenido tipo = es.plotgram.backend.entidades.TipoContenido.valueOf(tipoStr);
        return switch (tipo) {
            case PELICULA -> es.plotgram.backend.entidades.Pelicula.class;
            case SERIE -> es.plotgram.backend.entidades.Serie.class;
            case TEMPORADA -> es.plotgram.backend.entidades.Temporada.class;
            case EPISODIO -> es.plotgram.backend.entidades.Episodio.class;
        };
    }
}
