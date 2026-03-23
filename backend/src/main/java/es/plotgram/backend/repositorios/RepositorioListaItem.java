package es.plotgram.backend.repositorios;

import es.plotgram.backend.entidades.ListaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioListaItem extends JpaRepository<ListaItem, Long> {

    @Query("""
           select li
           from ListaItem li
           join fetch li.contenido
           where li.lista.id = :idLista
           order by li.orden asc, li.id asc
           """)
    List<ListaItem> buscarPorListaIdOrdenados(@Param("idLista") Long idLista);

    @Query("""
           select count(li)
           from ListaItem li
           where li.lista.id = :idLista
           """)
    long contarPorListaId(@Param("idLista") Long idLista);

    @Query("""
           select case when count(li) > 0 then true else false end
           from ListaItem li
           where li.lista.id = :idLista
             and li.contenido.id = :idContenido
           """)
    boolean existePorListaIdYContenidoId(@Param("idLista") Long idLista,
                                         @Param("idContenido") Long idContenido);

    @Query("""
           select max(li.orden)
           from ListaItem li
           where li.lista.id = :idLista
           """)
    Integer buscarUltimoOrdenDeLista(@Param("idLista") Long idLista);

    @Query("""
           select li
           from ListaItem li
           join fetch li.contenido
           where li.id = :idItem
             and li.lista.id = :idLista
           """)
    Optional<ListaItem> buscarPorIdYListaId(@Param("idItem") Long idItem,
                                            @Param("idLista") Long idLista);

    default ListaItem guardar(ListaItem listaItem) {
        return save(listaItem);
    }

    default void borrar(ListaItem listaItem) {
        delete(listaItem);
    }
}