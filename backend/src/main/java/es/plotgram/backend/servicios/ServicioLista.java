package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Contenido;
import es.plotgram.backend.entidades.Lista;
import es.plotgram.backend.entidades.ListaItem;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.excepciones.ContenidoYaEnLista;
import es.plotgram.backend.excepciones.ElementoNoEncontradoEnLista;
import es.plotgram.backend.excepciones.ListaNoEncontrada;
import es.plotgram.backend.excepciones.UsuarioNoEncontrado;
import es.plotgram.backend.repositorios.RepositorioContenido;
import es.plotgram.backend.repositorios.RepositorioLista;
import es.plotgram.backend.repositorios.RepositorioListaItem;
import es.plotgram.backend.repositorios.RepositorioUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class ServicioLista {

    private final RepositorioLista repositorioLista;
    private final RepositorioListaItem repositorioListaItem;
    private final RepositorioContenido repositorioContenido;
    private final RepositorioUsuario repositorioUsuario;

    public ServicioLista(RepositorioLista repositorioLista,
                         RepositorioListaItem repositorioListaItem,
                         RepositorioContenido repositorioContenido,
                         RepositorioUsuario repositorioUsuario) {
        this.repositorioLista = repositorioLista;
        this.repositorioListaItem = repositorioListaItem;
        this.repositorioContenido = repositorioContenido;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Transactional
    public ListaDetalleServicio crearLista(String nombreUsuario, Lista listaNueva) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);

        Lista lista = new Lista();
        lista.setNombre(listaNueva.getNombre());
        lista.setDescripcion(listaNueva.getDescripcion());
        lista.setImagenPortada(listaNueva.getImagenPortada());
        lista.setUsuario(usuario);

        Lista listaGuardada = repositorioLista.guardar(lista);
        return new ListaDetalleServicio(listaGuardada, List.of());
    }

    @Transactional(readOnly = true)
    public List<ListaResumenServicio> obtenerListasDelUsuario(String nombreUsuario) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);

        return repositorioLista.buscarPorUsuarioId(usuario.getId()).stream()
                .map(lista -> new ListaResumenServicio(
                        lista,
                        repositorioListaItem.contarPorListaId(lista.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ListaDetalleServicio obtenerLista(long idLista, String nombreUsuario) {
        Lista lista = obtenerListaDelUsuario(idLista, nombreUsuario);
        List<ListaItem> elementos = repositorioListaItem.buscarPorListaIdOrdenados(lista.getId());
        return new ListaDetalleServicio(lista, elementos);
    }

    @Transactional
    public ListaDetalleServicio aniadirContenido(long idLista, String nombreUsuario, Contenido contenidoNuevo) {
        Lista lista = obtenerListaDelUsuario(idLista, nombreUsuario);

        Contenido contenido = repositorioContenido.buscarPorTmdbIdYTipo(contenidoNuevo.getTmdbId(), contenidoNuevo.getTipo())
                .orElseGet(() -> repositorioContenido.guardar(contenidoNuevo));

        if (repositorioListaItem.existePorListaIdYContenidoId(lista.getId(), contenido.getId())) {
            throw new ContenidoYaEnLista();
        }

        Integer ultimoOrden = repositorioListaItem.buscarUltimoOrdenDeLista(lista.getId());

        ListaItem listaItem = new ListaItem();
        listaItem.setLista(lista);
        listaItem.setContenido(contenido);
        listaItem.setOrden(ultimoOrden == null ? 0 : ultimoOrden + 1);

        repositorioListaItem.guardar(listaItem);

        return obtenerLista(lista.getId(), nombreUsuario);
    }

    @Transactional
    public void eliminarElemento(long idLista, long idItem, String nombreUsuario) {
        Lista lista = obtenerListaDelUsuario(idLista, nombreUsuario);

        ListaItem listaItem = repositorioListaItem.buscarPorIdYListaId(idItem, lista.getId())
                .orElseThrow(ElementoNoEncontradoEnLista::new);

        repositorioListaItem.borrar(listaItem);
    }

    @Transactional
    public void eliminarLista(long idLista, String nombreUsuario) {
        Lista lista = obtenerListaDelUsuario(idLista, nombreUsuario);

        List<ListaItem> elementos = repositorioListaItem.buscarPorListaIdOrdenados(lista.getId());
        for (ListaItem item : elementos) {
            repositorioListaItem.borrar(item);
        }

        repositorioLista.borrar(lista);
    }

    @Transactional
    public ListaDetalleServicio editarLista(long idLista, String nombreUsuario, Lista datosLista) {
        Lista lista = obtenerListaDelUsuario(idLista, nombreUsuario);

        lista.setNombre(datosLista.getNombre());
        lista.setDescripcion(datosLista.getDescripcion());
        lista.setImagenPortada(datosLista.getImagenPortada());

        repositorioLista.guardar(lista);

        List<ListaItem> elementos = repositorioListaItem.buscarPorListaIdOrdenados(lista.getId());
        return new ListaDetalleServicio(lista, elementos);
    }

    private Usuario obtenerUsuarioPorNombre(String nombreUsuario) {
        return repositorioUsuario.buscarPorNombre(nombreUsuario)
                .orElseThrow(UsuarioNoEncontrado::new);
    }

    private Lista obtenerListaDelUsuario(long idLista, String nombreUsuario) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);

        return repositorioLista.buscarPorIdYUsuarioId(idLista, usuario.getId())
                .orElseThrow(ListaNoEncontrada::new);
    }

    public record ListaDetalleServicio(Lista lista, List<ListaItem> elementos) {}

    public record ListaResumenServicio(Lista lista, long totalElementos) {}
}
