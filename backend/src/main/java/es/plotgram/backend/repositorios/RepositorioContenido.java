package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.Contenido;
import es.plotgram.backend.entidades.TipoContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RepositorioContenido extends JpaRepository<Contenido, Long> {

    @Query("""
           select c
           from Contenido c
           where c.tmdbId = :tmdbId
             and c.tipo = :tipo
           """)
    Optional<Contenido> buscarPorTmdbIdYTipo(@Param("tmdbId") Long tmdbId,
                                             @Param("tipo") TipoContenido tipo);

    default Contenido guardar(Contenido contenido) {
        return save(contenido);
    }
}
