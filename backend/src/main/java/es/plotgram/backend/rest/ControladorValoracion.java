package es.plotgram.backend.rest;

import es.plotgram.backend.entidades.TipoContenido;
import es.plotgram.backend.rest.dto.Mapeador;
import es.plotgram.backend.rest.dto.valoraciones.DMediaValoracion;
import es.plotgram.backend.rest.dto.valoraciones.DValoracionNueva;
import es.plotgram.backend.rest.dto.valoraciones.DValoracionResumen;
import es.plotgram.backend.servicios.ServicioValoracion;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de la gestión de valoraciones y reseñas de contenidos (películas y series).
 * Permite a los usuarios puntuar contenidos, dejar comentarios y consultar las medias de valoración.
 */
@RestController
@RequestMapping("/api")
public class ControladorValoracion {

    private final ServicioValoracion servicioValoracion;
    private final Mapeador mapeador;

    public ControladorValoracion(ServicioValoracion servicioValoracion, Mapeador mapeador) {
        this.servicioValoracion = servicioValoracion;
        this.mapeador = mapeador;
    }

    /**
     * Registra una nueva valoración o actualiza una existente para un contenido.
     *
     * @param authentication Información de sesión del usuario que valora.
     * @param dto DTO con los datos de la valoración (contenido, puntuación, comentario).
     * @return DTO con el resumen de la valoración creada o actualizada.
     */
    @PostMapping("/valoraciones")
    public ResponseEntity<DValoracionResumen> valorar(Authentication authentication,
                                                       @Valid @RequestBody DValoracionNueva dto) {
        var valoracion = servicioValoracion.valorar(
                authentication.getName(),
                mapeador.entidadNueva(dto.contenido()),
                dto.puntuacion(),
                dto.comentario()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapeador.dto(valoracion));
    }

    /**
     * Recupera todas las valoraciones y reseñas de una película específica.
     *
     * @param id ID de la película en el sistema (TMDB ID).
     * @return Lista de resúmenes de valoraciones.
     */
    @GetMapping("/peliculas/{id}/valoraciones")
    public List<DValoracionResumen> obtenerValoracionesPelicula(@PathVariable Long id) {
        return servicioValoracion.obtenerValoraciones(id, TipoContenido.PELICULA).stream()
                .map(mapeador::dto)
                .toList();
    }

    /**
     * Obtiene la puntuación media y el total de votos para una película.
     *
     * @param id ID de la película.
     * @return DTO con la media y el conteo total.
     */
    @GetMapping("/peliculas/{id}/valoraciones/media")
    public DMediaValoracion obtenerMediaPelicula(@PathVariable Long id) {
        Double media = servicioValoracion.obtenerMedia(id, TipoContenido.PELICULA);
        long total = servicioValoracion.obtenerTotal(id, TipoContenido.PELICULA);
        return mapeador.dtoMedia(media, total);
    }

    /**
     * Recupera todas las valoraciones y reseñas de una serie específica.
     *
     * @param id ID de la serie en el sistema.
     * @return Lista de resúmenes de valoraciones.
     */
    @GetMapping("/series/{id}/valoraciones")
    public List<DValoracionResumen> obtenerValoracionesSerie(@PathVariable Long id) {
        return servicioValoracion.obtenerValoraciones(id, TipoContenido.SERIE).stream()
                .map(mapeador::dto)
                .toList();
    }

    /**
     * Obtiene la puntuación media y el total de votos para una serie.
     *
     * @param id ID de la serie.
     * @return DTO con la media y el conteo total.
     */
    @GetMapping("/series/{id}/valoraciones/media")
    public DMediaValoracion obtenerMediaSerie(@PathVariable Long id) {
        Double media = servicioValoracion.obtenerMedia(id, TipoContenido.SERIE);
        long total = servicioValoracion.obtenerTotal(id, TipoContenido.SERIE);
        return mapeador.dtoMedia(media, total);
    }
}
