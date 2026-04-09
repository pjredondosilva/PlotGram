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
    public List<ListaResumenServicio> obtenerMisListas(String nombreUsuario) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);
        return construirResumenesDeUsuario(usuario.getId());
    }

    @Transactional(readOnly = true)
    public List<ListaResumenServicio> obtenerListasDeUsuario(Long idUsuario) {
        Usuario usuario = obtenerUsuarioPorId(idUsuario);
        return construirResumenesDeUsuario(usuario.getId());
    }

    @Transactional(readOnly = true)
    public ListaDetalleServicio obtenerListaDeUsuario(Long idUsuario, Long idLista) {
        Lista lista = obtenerListaPorUsuarioId(idLista, idUsuario);
        List<ListaItem> elementos = repositorioListaItem.buscarPorListaIdOrdenados(lista.getId());
        return new ListaDetalleServicio(lista, elementos);
    }

    @Transactional
    public ListaDetalleServicio aniadirContenido(Long idLista, String nombreUsuario, Contenido contenidoNuevo) {
        Lista lista = obtenerListaDelUsuarioAutenticado(idLista, nombreUsuario);

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

        List<ListaItem> elementos = repositorioListaItem.buscarPorListaIdOrdenados(lista.getId());
        return new ListaDetalleServicio(lista, elementos);
    }

    @Transactional
    public void eliminarElemento(Long idLista, Long idItem, String nombreUsuario) {
        Lista lista = obtenerListaDelUsuarioAutenticado(idLista, nombreUsuario);

        ListaItem listaItem = repositorioListaItem.buscarPorIdYListaId(idItem, lista.getId())
                .orElseThrow(ElementoNoEncontradoEnLista::new);

        repositorioListaItem.borrar(listaItem);
    }

    @Transactional
    public void eliminarLista(Long idLista, String nombreUsuario) {
        Lista lista = obtenerListaDelUsuarioAutenticado(idLista, nombreUsuario);

        List<ListaItem> elementos = repositorioListaItem.buscarPorListaIdOrdenados(lista.getId());
        for (ListaItem item : elementos) {
            repositorioListaItem.borrar(item);
        }

        repositorioLista.borrar(lista);
    }

    @Transactional
    public ListaDetalleServicio editarLista(Long idLista, String nombreUsuario, Lista datosLista) {
        Lista lista = obtenerListaDelUsuarioAutenticado(idLista, nombreUsuario);

        lista.setNombre(datosLista.getNombre());
        lista.setDescripcion(datosLista.getDescripcion());
        lista.setImagenPortada(datosLista.getImagenPortada());

        repositorioLista.guardar(lista);

        List<ListaItem> elementos = repositorioListaItem.buscarPorListaIdOrdenados(lista.getId());
        return new ListaDetalleServicio(lista, elementos);
    }

    private List<ListaResumenServicio> construirResumenesDeUsuario(Long usuarioId) {
        return repositorioLista.buscarPorUsuarioId(usuarioId).stream()
                .map(lista -> new ListaResumenServicio(
                        lista,
                        repositorioListaItem.contarPorListaId(lista.getId())
                ))
                .toList();
    }

    private Usuario obtenerUsuarioPorNombre(String nombreUsuario) {
        return repositorioUsuario.buscarPorNombre(nombreUsuario)
                .orElseThrow(UsuarioNoEncontrado::new);
    }

    private Usuario obtenerUsuarioPorId(Long idUsuario) {
        return repositorioUsuario.buscarPorID(idUsuario)
                .orElseThrow(UsuarioNoEncontrado::new);
    }

    private Lista obtenerListaDelUsuarioAutenticado(Long idLista, String nombreUsuario) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);
        return obtenerListaPorUsuarioId(idLista, usuario.getId());
    }

    private Lista obtenerListaPorUsuarioId(Long idLista, Long idUsuario) {
        return repositorioLista.buscarPorIdYUsuarioId(idLista, idUsuario)
                .orElseThrow(ListaNoEncontrada::new);
    }

    public record ListaDetalleServicio(Lista lista, List<ListaItem> elementos) {}

    public record ListaResumenServicio(Lista lista, Long totalElementos) {}
}