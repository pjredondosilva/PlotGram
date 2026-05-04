package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Contenido;
import es.plotgram.backend.entidades.TipoContenido;
import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.entidades.Valoracion;
import es.plotgram.backend.excepciones.UsuarioNoEncontrado;
import es.plotgram.backend.repositorios.RepositorioValoracion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de gestionar la lógica de negocio relacionada con las valoraciones y reseñas.
 * Permite realizar valoraciones, consultar medias y listar reseñas de usuarios.
 */
@Service
public class ServicioValoracion {

    private final RepositorioValoracion repositorioValoracion;
    private final ServicioUsuario servicioUsuario;
    private final ServicioContenido servicioContenido;

    public ServicioValoracion(RepositorioValoracion repositorioValoracion,
                               ServicioUsuario servicioUsuario,
                               ServicioContenido servicioContenido) {
        this.repositorioValoracion = repositorioValoracion;
        this.servicioUsuario = servicioUsuario;
        this.servicioContenido = servicioContenido;
    }

    /**
     * Registra o actualiza una valoración de un usuario sobre un contenido.
     * 
     * @param nombreUsuario Nombre del usuario que realiza la valoración.
     * @param contenidoDetalle Objeto con los detalles del contenido a valorar.
     * @param puntuacion Nota numérica de 1 a 5.
     * @param comentario Texto opcional de la reseña.
     * @return La valoración guardada o actualizada.
     */
    @Transactional
    public Valoracion valorar(String nombreUsuario, Contenido contenidoDetalle, int puntuacion, String comentario) {
        Usuario usuario = servicioUsuario.buscarUsuario(nombreUsuario)
                .orElseThrow(UsuarioNoEncontrado::new);

        Contenido contenido = servicioContenido.buscarOGuardar(contenidoDetalle);

        // Si ya existe, la actualizamos. Si no, creamos una nueva.
        Optional<Valoracion> existente = repositorioValoracion.buscarPorUsuarioYContenido(
                usuario.getId(),
                contenido.getTmdbId(),
                contenido.getTipo().name()
        );

        Valoracion valoracion = existente.orElseGet(Valoracion::new);
        valoracion.setUsuario(usuario);
        valoracion.setContenido(contenido);
        valoracion.setPuntuacion(puntuacion);
        valoracion.setComentario(comentario);
        valoracion.setFecha(LocalDateTime.now());

        return repositorioValoracion.save(valoracion);
    }

    /**
     * Obtiene la lista de valoraciones para un contenido específico.
     * 
     * @param tmdbId ID del contenido en TMDB.
     * @param tipo Tipo de contenido (PELICULA/SERIE).
     * @return Lista de valoraciones encontradas.
     */
    @Transactional(readOnly = true)
    public List<Valoracion> obtenerValoraciones(Long tmdbId, TipoContenido tipo) {
        return repositorioValoracion.buscarPorTmdbIdYTipo(tmdbId, tipo.name());
    }

    /**
     * Calcula la puntuación media de un contenido.
     * 
     * @param tmdbId ID del contenido en TMDB.
     * @param tipo Tipo de contenido (PELICULA/SERIE).
     * @return Media aritmética de las puntuaciones o 0.0 si no hay votos.
     */
    @Transactional(readOnly = true)
    public Double obtenerMedia(Long tmdbId, TipoContenido tipo) {
        Double media = repositorioValoracion.calcularMediaPorTmdbIdYTipo(tmdbId, tipo.name());
        return media != null ? media : 0.0;
    }

    /**
     * Cuenta el número total de valoraciones de un contenido.
     * 
     * @param tmdbId ID del contenido en TMDB.
     * @param tipo Tipo de contenido (PELICULA/SERIE).
     * @return Cantidad de votos registrados.
     */
    @Transactional(readOnly = true)
    public long obtenerTotal(Long tmdbId, TipoContenido tipo) {
        return repositorioValoracion.contarPorTmdbIdYTipo(tmdbId, tipo.name());
    }
}
