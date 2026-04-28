package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Contenido;
import es.plotgram.backend.entidades.Lista;
import es.plotgram.backend.entidades.ListaItem;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.excepciones.*;
import es.plotgram.backend.repositorios.RepositorioLista;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Validated
public class ServicioLista {

    private final RepositorioLista repositorioLista;
    private final ServicioListaItem servicioListaItem;
    private final ServicioContenido servicioContenido;
    private final ServicioUsuario servicioUsuario;

    public ServicioLista(RepositorioLista repositorioLista,
                         ServicioListaItem servicioListaItem,
                         ServicioContenido servicioContenido,
                         ServicioUsuario servicioUsuario) {
        this.repositorioLista = repositorioLista;
        this.servicioListaItem = servicioListaItem;
        this.servicioContenido = servicioContenido;
        this.servicioUsuario = servicioUsuario;
    }

    @Transactional
    public ListaDetalleServicio crearLista(String nombreUsuario, Lista listaNueva) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);

        String nombreNormalizado = normalizarNombreLista(listaNueva.getNombre());
        validarNombreDisponible(usuario.getId(), nombreNormalizado, null);

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
        List<ListaItem> elementos = servicioListaItem.buscarPorListaIdOrdenados(lista.getId());
        return new ListaDetalleServicio(lista, elementos);
    }

    @Transactional
    public ListaDetalleServicio aniadirContenido(Long idLista, String nombreUsuario, Contenido contenidoNuevo) {
        Lista lista = obtenerListaDelUsuarioAutenticado(idLista, nombreUsuario);

        Contenido contenido = servicioContenido.buscarOGuardar(contenidoNuevo);

        if (servicioListaItem.existePorListaIdYContenidoId(lista.getId(), contenido.getId())) {
            throw new ContenidoYaEnLista();
        }

        Integer ultimoOrden = servicioListaItem.buscarUltimoOrdenDeLista(lista.getId());

        ListaItem listaItem = new ListaItem();
        listaItem.setLista(lista);
        listaItem.setContenido(contenido);
        listaItem.setOrden(ultimoOrden == null ? 0 : ultimoOrden + 1);

        servicioListaItem.guardar(listaItem);

        List<ListaItem> elementos = servicioListaItem.buscarPorListaIdOrdenados(lista.getId());
        return new ListaDetalleServicio(lista, elementos);
    }

    @Transactional
    public void eliminarElemento(Long idLista, Long idItem, String nombreUsuario) {
        Lista lista = obtenerListaDelUsuarioAutenticado(idLista, nombreUsuario);

        ListaItem listaItem = servicioListaItem.buscarPorIdYListaId(idItem, lista.getId())
                .orElseThrow(ElementoNoEncontradoEnLista::new);

        servicioListaItem.borrar(listaItem);
    }

    @Transactional
    public void eliminarLista(Long idLista, String nombreUsuario) {
        Lista lista = obtenerListaDelUsuarioAutenticado(idLista, nombreUsuario);

        List<ListaItem> elementos = servicioListaItem.buscarPorListaIdOrdenados(lista.getId());
        for (ListaItem item : elementos) {
            servicioListaItem.borrar(item);
        }

        repositorioLista.borrar(lista);
    }

    @Transactional
    public ListaDetalleServicio editarLista(Long idLista, String nombreUsuario, Lista datosLista) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);
        Lista lista = obtenerListaDelUsuarioAutenticado(idLista, nombreUsuario);

        String nombreNormalizado = normalizarNombreLista(datosLista.getNombre());
        validarNombreDisponible(usuario.getId(), nombreNormalizado, lista.getId());

        lista.setNombre(datosLista.getNombre());
        lista.setDescripcion(datosLista.getDescripcion());
        lista.setImagenPortada(datosLista.getImagenPortada());

        Lista listaActualizada = repositorioLista.actualizar(lista);

        List<ListaItem> elementos = servicioListaItem.buscarPorListaIdOrdenados(listaActualizada.getId());
        return new ListaDetalleServicio(listaActualizada, elementos);
    }

    private void validarNombreDisponible(Long usuarioId, String nombre, Long idListaActual) {
        boolean nombreEnUso = idListaActual == null
                ? repositorioLista.existePorUsuarioIdYNombre(usuarioId, nombre)
                : repositorioLista.existePorUsuarioIdYNombreEIdDistinto(usuarioId, nombre, idListaActual);

        if (nombreEnUso) {
            throw new ListaYaRegistrada("nombre");
        }
    }

    private String normalizarNombreLista(String nombre) {
        String nombreNormalizado = nombre == null ? "" : nombre.trim();

        if (nombreNormalizado.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre de la lista es obligatorio."
            );
        }

        return nombreNormalizado;
    }

    private List<ListaResumenServicio> construirResumenesDeUsuario(Long usuarioId) {
        return repositorioLista.buscarPorUsuarioId(usuarioId).stream()
                .map(lista -> new ListaResumenServicio(
                        lista,
                        servicioListaItem.contarPorListaId(lista.getId())
                ))
                .toList();
    }

    private Usuario obtenerUsuarioPorNombre(String nombreUsuario) {
        return servicioUsuario.buscarUsuario(nombreUsuario)
                .orElseThrow(UsuarioNoEncontrado::new);
    }

    private Usuario obtenerUsuarioPorId(Long idUsuario) {
        return servicioUsuario.buscarUsuario(idUsuario)
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