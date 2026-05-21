package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.ListaItem;
import es.plotgram.backend.repositorios.RepositorioListaItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de gestionar los elementos individuales de las listas.
 * Controla el orden y la vinculación entre listas y contenidos.
 */
@Service
@Validated
public class ServicioListaItem {

    private final RepositorioListaItem repositorioListaItem;

    public ServicioListaItem(RepositorioListaItem repositorioListaItem) {
        this.repositorioListaItem = repositorioListaItem;
    }

    /**
     * Busca y obtiene los elementos de una lista ordenados por su campo de orden.
     *
     * @param listaId ID de la lista.
     * @return Lista de elementos (ListaItem) ordenados.
     */
    public List<ListaItem> buscarPorListaIdOrdenados(Long listaId) {
        return repositorioListaItem.buscarPorListaIdOrdenados(listaId);
    }

    /**
     * Comprueba si un contenido ya existe dentro de una lista de reproducción.
     *
     * @param listaId ID de la lista.
     * @param contenidoId ID del contenido.
     * @return true si el contenido ya está en la lista, false en caso contrario.
     */
    public boolean existePorListaIdYContenidoId(Long listaId, Long contenidoId) {
        return repositorioListaItem.existePorListaIdYContenidoId(listaId, contenidoId);
    }

    /**
     * Obtiene el valor del último orden asignado en una lista de reproducción.
     *
     * @param listaId ID de la lista.
     * @return El número de orden más alto, o null si la lista está vacía.
     */
    public Integer buscarUltimoOrdenDeLista(Long listaId) {
        return repositorioListaItem.buscarUltimoOrdenDeLista(listaId);
    }

    /**
     * Guarda o actualiza un elemento de lista en el repositorio.
     *
     * @param listaItem Elemento a guardar.
     * @return El elemento guardado con su ID generado.
     */
    public ListaItem guardar(ListaItem listaItem) {
        return repositorioListaItem.guardar(listaItem);
    }

    /**
     * Busca un elemento específico dentro de una lista concreta.
     *
     * @param idItem ID del elemento a buscar.
     * @param listaId ID de la lista que lo contiene.
     * @return Un Optional con el elemento si es encontrado.
     */
    public Optional<ListaItem> buscarPorIdYListaId(Long idItem, Long listaId) {
        return repositorioListaItem.buscarPorIdYListaId(idItem, listaId);
    }

    /**
     * Elimina un elemento de la lista de reproducción.
     *
     * @param listaItem Elemento a eliminar.
     */
    public void borrar(ListaItem listaItem) {
        repositorioListaItem.borrar(listaItem);
    }

    /**
     * Cuenta el número total de elementos contenidos en una lista de reproducción.
     *
     * @param listaId ID de la lista.
     * @return Total de elementos en la lista.
     */
    public Long contarPorListaId(Long listaId) {
        return repositorioListaItem.contarPorListaId(listaId);
    }
}