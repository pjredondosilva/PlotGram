package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.ListaItem;
import es.plotgram.backend.repositorios.RepositorioListaItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
public class ServicioListaItem {

    private final RepositorioListaItem repositorioListaItem;

    public ServicioListaItem(RepositorioListaItem repositorioListaItem) {
        this.repositorioListaItem = repositorioListaItem;
    }

    public List<ListaItem> buscarPorListaIdOrdenados(Long listaId) {
        return repositorioListaItem.buscarPorListaIdOrdenados(listaId);
    }

    public boolean existePorListaIdYContenidoId(Long listaId, Long contenidoId) {
        return repositorioListaItem.existePorListaIdYContenidoId(listaId, contenidoId);
    }

    public Integer buscarUltimoOrdenDeLista(Long listaId) {
        return repositorioListaItem.buscarUltimoOrdenDeLista(listaId);
    }

    public ListaItem guardar(ListaItem listaItem) {
        return repositorioListaItem.guardar(listaItem);
    }

    public Optional<ListaItem> buscarPorIdYListaId(Long idItem, Long listaId) {
        return repositorioListaItem.buscarPorIdYListaId(idItem, listaId);
    }

    public void borrar(ListaItem listaItem) {
        repositorioListaItem.borrar(listaItem);
    }

    public Long contarPorListaId(Long listaId) {
        return repositorioListaItem.contarPorListaId(listaId);
    }
}