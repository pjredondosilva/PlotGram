package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Contenido;
import es.plotgram.backend.entidades.Episodio;
import es.plotgram.backend.entidades.Lista;
import es.plotgram.backend.entidades.ListaItem;
import es.plotgram.backend.entidades.Pelicula;
import es.plotgram.backend.entidades.Serie;
import es.plotgram.backend.entidades.Temporada;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.excepciones.ContenidoYaEnLista;
import es.plotgram.backend.excepciones.ElementoNoEncontradoEnLista;
import es.plotgram.backend.excepciones.ListaNoEncontrada;
import es.plotgram.backend.excepciones.UsuarioNoEncontrado;
import es.plotgram.backend.repositorios.RepositorioContenido;
import es.plotgram.backend.repositorios.RepositorioLista;
import es.plotgram.backend.repositorios.RepositorioListaItem;
import es.plotgram.backend.repositorios.RepositorioUsuario;
import es.plotgram.backend.rest.dto.DContenidoListaNuevo;
import es.plotgram.backend.rest.dto.DListaDetalle;
import es.plotgram.backend.rest.dto.DListaElemento;
import es.plotgram.backend.rest.dto.DListaNueva;
import es.plotgram.backend.rest.dto.DListaResumen;
import jakarta.validation.Valid;
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
    public DListaDetalle crearLista(String nombreUsuario, @Valid DListaNueva dto) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);

        Lista lista = new Lista();
        lista.setNombre(dto.nombre());
        lista.setDescripcion(dto.descripcion());
        lista.setImagenPortada(dto.imagenPortada());
        lista.setUsuario(usuario);

        Lista listaGuardada = repositorioLista.guardar(lista);
        return dtoDetalle(listaGuardada, List.of());
    }

    @Transactional(readOnly = true)
    public List<DListaResumen> obtenerListasDelUsuario(String nombreUsuario) {
        Usuario usuario = obtenerUsuarioPorNombre(nombreUsuario);

        return repositorioLista.buscarPorUsuarioId(usuario.getId()).stream()
                .map(lista -> new DListaResumen(
                        lista.getId(),
                        lista.getNombre(),
                        lista.getDescripcion(),
                        lista.getImagenPortada(),
                        repositorioListaItem.contarPorListaId(lista.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public DListaDetalle obtenerLista(long idLista, String nombreUsuario) {
        Lista lista = obtenerListaDelUsuario(idLista, nombreUsuario);
        List<ListaItem> elementos = repositorioListaItem.buscarPorListaIdOrdenados(lista.getId());
        return dtoDetalle(lista, elementos);
    }

    @Transactional
    public DListaDetalle aniadirContenido(long idLista, String nombreUsuario, @Valid DContenidoListaNuevo dto) {
        Lista lista = obtenerListaDelUsuario(idLista, nombreUsuario);

        Contenido contenido = repositorioContenido.buscarPorTmdbIdYTipo(dto.tmdbId(), dto.tipo())
                .orElseGet(() -> crearContenido(dto));

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
    public DListaDetalle editarLista(long idLista, String nombreUsuario, @Valid DListaNueva dto) {
        Lista lista = obtenerListaDelUsuario(idLista, nombreUsuario);

        lista.setNombre(dto.nombre());
        lista.setDescripcion(dto.descripcion());
        lista.setImagenPortada(dto.imagenPortada());

        repositorioLista.guardar(lista);

        List<ListaItem> elementos = repositorioListaItem.buscarPorListaIdOrdenados(lista.getId());
        return dtoDetalle(lista, elementos);
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

    private Contenido crearContenido(DContenidoListaNuevo dto) {
        Contenido contenido = switch (dto.tipo()) {
            case PELICULA -> new Pelicula();
            case SERIE -> new Serie();
            case TEMPORADA -> {
                Temporada temporada = new Temporada();
                temporada.setSerieTmdbId(dto.serieTmdbId());
                temporada.setNumeroTemporada(dto.numeroTemporada());
                yield temporada;
            }
            case EPISODIO -> {
                Episodio episodio = new Episodio();
                episodio.setSerieTmdbId(dto.serieTmdbId());
                episodio.setNumeroTemporada(dto.numeroTemporada());
                episodio.setNumeroEpisodio(dto.numeroEpisodio());
                yield episodio;
            }
        };

        aplicarDatosComunes(contenido, dto);
        return repositorioContenido.guardar(contenido);
    }

    private void aplicarDatosComunes(Contenido contenido, DContenidoListaNuevo dto) {
        contenido.setTmdbId(dto.tmdbId());
        contenido.setTitulo(dto.titulo());
        contenido.setImagen(dto.imagen());
        contenido.setFechaPublicacion(dto.fechaPublicacion());
        contenido.setSinopsis(dto.sinopsis());
        contenido.setEnlace(dto.enlace());
    }

    private DListaDetalle dtoDetalle(Lista lista, List<ListaItem> elementos) {
        return new DListaDetalle(
                lista.getId(),
                lista.getNombre(),
                lista.getDescripcion(),
                lista.getImagenPortada(),
                elementos.stream().map(this::dtoElemento).toList()
        );
    }

    private DListaElemento dtoElemento(ListaItem item) {
        Contenido contenido = item.getContenido();

        return new DListaElemento(
                item.getId(),
                item.getOrden(),
                contenido.getId(),
                contenido.getTmdbId(),
                contenido.getTipo(),
                contenido.getTitulo(),
                contenido.getImagen(),
                contenido.getFechaPublicacion(),
                contenido.getSinopsis(),
                contenido.getEnlace(),
                contenido.getSerieTmdbId(),
                contenido.getNumeroTemporada(),
                contenido.getNumeroEpisodio()
        );
    }
}