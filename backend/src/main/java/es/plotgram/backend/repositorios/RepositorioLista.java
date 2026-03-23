package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.Lista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioLista extends JpaRepository<Lista, Long> {

    @Query("""
           select l
           from Lista l
           where l.usuario.id = :usuarioId
           order by l.id desc
           """)
    List<Lista> buscarPorUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("""
           select l
           from Lista l
           where l.id = :idLista
             and l.usuario.id = :usuarioId
           """)
    Optional<Lista> buscarPorIdYUsuarioId(@Param("idLista") Long idLista,
                                          @Param("usuarioId") Long usuarioId);

    default Lista guardar(Lista lista) {
        return save(lista);
    }

    default void borrar(Lista lista) {
        delete(lista);
    }
}