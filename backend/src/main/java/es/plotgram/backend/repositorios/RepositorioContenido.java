package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.Contenido;
import es.plotgram.backend.entidades.Episodio;
import es.plotgram.backend.entidades.Pelicula;
import es.plotgram.backend.entidades.Serie;
import es.plotgram.backend.entidades.Temporada;
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
        Class<? extends Contenido> clase = resolverClase(tipo);

        var q = em.createQuery("""
                SELECT c
                FROM Contenido c
                WHERE c.tmdbId = :tmdbId
                  AND TYPE(c) = :clase
                """, Contenido.class);

        q.setParameter("tmdbId", tmdbId);
        q.setParameter("clase", clase);
        q.setMaxResults(1);

        return q.getResultList().stream().findFirst();
    }

    private Class<? extends Contenido> resolverClase(TipoContenido tipo) {
        return switch (tipo) {
            case PELICULA -> Pelicula.class;
            case SERIE -> Serie.class;
            case TEMPORADA -> Temporada.class;
            case EPISODIO -> Episodio.class;
        };
    }
}