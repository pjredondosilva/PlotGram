package es.plotgram.backend.rest.dto;

import es.plotgram.backend.entidades.Contenido;
import es.plotgram.backend.entidades.Episodio;
import es.plotgram.backend.entidades.Lista;
import es.plotgram.backend.entidades.ListaItem;
import es.plotgram.backend.entidades.Pelicula;
import es.plotgram.backend.entidades.Serie;
import es.plotgram.backend.entidades.Temporada;
import es.plotgram.backend.entidades.Tipousuario;
import es.plotgram.backend.entidades.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Mapeador {

    private final PasswordEncoder codificadorClaves;

    public Mapeador(PasswordEncoder codificadorClaves) {
        this.codificadorClaves = codificadorClaves;
    }

    public Dusuario dto(Usuario usuario) {
        return dto(usuario, null);
    }

    public Dusuario dto(Usuario usuario, Boolean loSigo) {
        return new Dusuario(
                usuario.getId(),
                usuario.getNombre(),
                null,
                usuario.getEmail(),
                usuario.getFotoPerfil(),
                usuario.getDescripcion(),
                usuario.getTipo(),
                usuario.isBorrado(),
                usuario.getSeguidores().size(),
                usuario.getSeguidos().size(),
                loSigo
        );
    }

    public Usuario entidad(Dusuario dUsuario) {
        return new Usuario(
                dUsuario.id(),
                dUsuario.nombre(),
                dUsuario.contrasenia(),
                dUsuario.email(),
                dUsuario.fotoPerfil(),
                dUsuario.descripcion(),
                dUsuario.tipo(),
                dUsuario.borrado()
        );
    }

    public Usuario entidadNueva(DUsuarioRegistro dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre());
        usuario.setContrasena(codificadorClaves.encode(dto.contrasenia()));
        usuario.setEmail(dto.email());
        usuario.setFotoPerfil(null);
        usuario.setDescripcion(null);
        usuario.setTipo(Tipousuario.USER);
        usuario.setBorrado(false);
        return usuario;
    }

    public Lista entidadNueva(DListaNueva dto) {
        Lista lista = new Lista();
        lista.setNombre(dto.nombre());
        lista.setDescripcion(dto.descripcion());
        lista.setImagenPortada(dto.imagenPortada());
        return lista;
    }

    public Contenido entidadNueva(DContenidoListaNuevo dto) {
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

        contenido.setTmdbId(dto.tmdbId());
        contenido.setTitulo(dto.titulo());
        contenido.setImagen(dto.imagen());
        contenido.setFechaPublicacion(dto.fechaPublicacion());
        contenido.setSinopsis(dto.sinopsis());
        contenido.setEnlace(dto.enlace());

        return contenido;
    }

    public DListaResumen dtoResumen(Lista lista, long totalElementos) {
        return new DListaResumen(
                lista.getId(),
                lista.getNombre(),
                lista.getDescripcion(),
                lista.getImagenPortada(),
                totalElementos
        );
    }

    public DListaDetalle dtoDetalle(Lista lista, List<ListaItem> elementos) {
        return new DListaDetalle(
                lista.getId(),
                lista.getNombre(),
                lista.getDescripcion(),
                lista.getImagenPortada(),
                elementos.stream().map(this::dtoElemento).toList()
        );
    }

    public DListaElemento dtoElemento(ListaItem item) {
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

    public es.plotgram.backend.rest.dto.valoraciones.DValoracionResumen dto(es.plotgram.backend.entidades.Valoracion valoracion) {
        Usuario u = valoracion.getUsuario();
        return new es.plotgram.backend.rest.dto.valoraciones.DValoracionResumen(
                valoracion.getId(),
                u.getId(),
                u.getNombre(),
                u.getFotoPerfil(),
                valoracion.getPuntuacion(),
                valoracion.getComentario(),
                valoracion.getFecha()
        );
    }

    public es.plotgram.backend.rest.dto.valoraciones.DMediaValoracion dtoMedia(Double media, long total) {
        return new es.plotgram.backend.rest.dto.valoraciones.DMediaValoracion(media, total);
    }

    public String contrasenaActual(DVerificacionContrasena dto) {
        return dto.contrasenaActual();
    }

    public DatosActualizacionPerfil datosActualizacionPerfil(DActualizacionPerfil dto) {
        return new DatosActualizacionPerfil(
                dto.nombre() == null ? null : dto.nombre().trim(),
                dto.email() == null ? null : dto.email().trim(),
                normalizarOpcional(dto.fotoPerfil()),
                normalizarOpcional(dto.descripcion()),
                dto.contrasenaActual(),
                normalizarOpcional(dto.nuevaContrasena())
        );
    }

    public record DatosActualizacionPerfil(
            String nombre,
            String email,
            String fotoPerfil,
            String descripcion,
            String contrasenaActual,
            String nuevaContrasena
    ) {}

    private String normalizarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isBlank() ? null : limpio;
    }
}
