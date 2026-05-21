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

/**
 * Servicio encargado de gestionar las listas de reproducción de los usuarios.
 * Permite crear, editar, borrar y consultar listas personalizadas y sus contenidos.
 */
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

    /**
     * Crea una nueva lista para un usuario.
     * 
     * @param nombreUsuario Nombre del propietario.
     * @param listaNueva Datos de la nueva lista.
     * @return Detalle de la lista recién creada.
     */
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

    /**
     * Obtiene el resumen de todas las listas del usuario autenticado.
     * 
     * @param nombreUsuario Nombre del usuario.
     * @return Lista de resúmenes con conteo de elementos.
     */
    @Transactional(readOnly = true)
    public List<ListaResumenServicio> obtenerMisListas(String nombreUsuario) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);
        return construirResumenesDeUsuario(usuario.getId());
    }

    /**
     * Obtiene el resumen de las listas de cualquier usuario por su ID.
     * 
     * @param idUsuario ID del usuario a consultar.
     * @return Lista de resúmenes de sus listas.
     */
    @Transactional(readOnly = true)
    public List<ListaResumenServicio> obtenerListasDeUsuario(Long idUsuario) {
        Usuario usuario = obtenerUsuarioPorId(idUsuario);
        return construirResumenesDeUsuario(usuario.getId());
    }

    /**
     * Obtiene el detalle completo de una lista específica de un usuario.
     * 
     * @param idUsuario ID del propietario.
     * @param idLista ID de la lista.
     * @return Detalle de la lista con sus elementos.
     */
    @Transactional(readOnly = true)
    public ListaDetalleServicio obtenerListaDeUsuario(Long idUsuario, Long idLista) {
        Lista lista = obtenerListaPorUsuarioId(idLista, idUsuario);
        List<ListaItem> elementos = servicioListaItem.buscarPorListaIdOrdenados(lista.getId());
        return new ListaDetalleServicio(lista, elementos);
    }

    /**
     * Añade un nuevo contenido a una lista.
     * 
     * @param idLista ID de la lista.
     * @param nombreUsuario Nombre del propietario que realiza la acción.
     * @param contenidoNuevo Detalles del contenido a añadir.
     * @return Detalle actualizado de la lista.
     */
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

    /**
     * Elimina un elemento específico de una lista.
     * 
     * @param idLista ID de la lista.
     * @param idItem ID del elemento a eliminar.
     * @param nombreUsuario Nombre del propietario.
     */
    @Transactional
    public void eliminarElemento(Long idLista, Long idItem, String nombreUsuario) {
        Lista lista = obtenerListaDelUsuarioAutenticado(idLista, nombreUsuario);

        ListaItem listaItem = servicioListaItem.buscarPorIdYListaId(idItem, lista.getId())
                .orElseThrow(ElementoNoEncontradoEnLista::new);

        servicioListaItem.borrar(listaItem);
    }

    /**
     * Elimina una lista completa junto con todos sus elementos.
     * 
     * @param idLista ID de la lista a borrar.
     * @param nombreUsuario Nombre del propietario.
     */
    @Transactional
    public void eliminarLista(Long idLista, String nombreUsuario) {
        Lista lista = obtenerListaDelUsuarioAutenticado(idLista, nombreUsuario);

        List<ListaItem> elementos = servicioListaItem.buscarPorListaIdOrdenados(lista.getId());
        for (ListaItem item : elementos) {
            servicioListaItem.borrar(item);
        }

        repositorioLista.borrar(lista);
    }

    /**
     * Edita los datos básicos de una lista (nombre, descripción, imagen).
     * 
     * @param idLista ID de la lista.
     * @param nombreUsuario Nombre del propietario.
     * @param datosLista Nuevos datos.
     * @return Detalle actualizado de la lista.
     */
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

    /**
     * Valida si un nombre de lista está disponible para un usuario.
     *
     * @param usuarioId ID del usuario propietario.
     * @param nombre Nombre de la lista a validar.
     * @param idListaActual ID de la lista actual (para exclusión en ediciones), o null en creaciones.
     * @throws ListaYaRegistrada Si el nombre ya está en uso por otra lista del usuario.
     */
    private void validarNombreDisponible(Long usuarioId, String nombre, Long idListaActual) {
        boolean nombreEnUso = idListaActual == null
                ? repositorioLista.existePorUsuarioIdYNombre(usuarioId, nombre)
                : repositorioLista.existePorUsuarioIdYNombreEIdDistinto(usuarioId, nombre, idListaActual);

        if (nombreEnUso) {
            throw new ListaYaRegistrada("nombre");
        }
    }

    /**
     * Limpia y normaliza el nombre de la lista, validando que no esté en blanco.
     *
     * @param nombre Nombre original de la lista.
     * @return Nombre normalizado.
     * @throws ResponseStatusException Si el nombre está vacío o en blanco (400 Bad Request).
     */
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

    /**
     * Construye los resúmenes de las listas de un usuario, incluyendo el conteo de elementos.
     *
     * @param usuarioId ID del usuario.
     * @return Lista de objetos de resumen.
     */
    private List<ListaResumenServicio> construirResumenesDeUsuario(Long usuarioId) {
        return repositorioLista.buscarPorUsuarioId(usuarioId).stream()
                .map(lista -> new ListaResumenServicio(
                        lista,
                        servicioListaItem.contarPorListaId(lista.getId())
                ))
                .toList();
    }

    /**
     * Obtiene una entidad Usuario por su nombre de usuario.
     *
     * @param nombreUsuario Nombre del usuario.
     * @return Entidad Usuario encontrada.
     * @throws UsuarioNoEncontrado Si el usuario no existe.
     */
    private Usuario obtenerUsuarioPorNombre(String nombreUsuario) {
        return servicioUsuario.buscarUsuario(nombreUsuario)
                .orElseThrow(UsuarioNoEncontrado::new);
    }

    /**
     * Obtiene una entidad Usuario por su ID.
     *
     * @param idUsuario ID del usuario.
     * @return Entidad Usuario encontrada.
     * @throws UsuarioNoEncontrado Si el usuario no existe.
     */
    private Usuario obtenerUsuarioPorId(Long idUsuario) {
        return servicioUsuario.buscarUsuario(idUsuario)
                .orElseThrow(UsuarioNoEncontrado::new);
    }

    /**
     * Obtiene una lista asegurando que pertenezca al usuario autenticado.
     *
     * @param idLista ID de la lista.
     * @param nombreUsuario Nombre del usuario autenticado.
     * @return Entidad Lista correspondiente.
     */
    private Lista obtenerListaDelUsuarioAutenticado(Long idLista, String nombreUsuario) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);
        return obtenerListaPorUsuarioId(idLista, usuario.getId());
    }

    /**
     * Obtiene una lista por su ID y el ID de su usuario propietario.
     *
     * @param idLista ID de la lista.
     * @param idUsuario ID del usuario propietario.
     * @return Entidad Lista correspondiente.
     * @throws ListaNoEncontrada Si la lista no existe para ese usuario.
     */
    private Lista obtenerListaPorUsuarioId(Long idLista, Long idUsuario) {
        return repositorioLista.buscarPorIdYUsuarioId(idLista, idUsuario)
                .orElseThrow(ListaNoEncontrada::new);
    }

    public record ListaDetalleServicio(Lista lista, List<ListaItem> elementos) {}

    public record ListaResumenServicio(Lista lista, Long totalElementos) {}
}