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
 * Controlador REST para la gestión de valoraciones y reseñas de películas y series.
 * Sigue una jerarquía de rutas coherente con el tipo de contenido.
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

    @GetMapping("/peliculas/{id}/valoraciones")
    public List<DValoracionResumen> obtenerValoracionesPelicula(@PathVariable Long id) {
        return servicioValoracion.obtenerValoraciones(id, TipoContenido.PELICULA).stream()
                .map(mapeador::dto)
                .toList();
    }

    @GetMapping("/peliculas/{id}/valoraciones/media")
    public DMediaValoracion obtenerMediaPelicula(@PathVariable Long id) {
        Double media = servicioValoracion.obtenerMedia(id, TipoContenido.PELICULA);
        long total = servicioValoracion.obtenerTotal(id, TipoContenido.PELICULA);
        return mapeador.dtoMedia(media, total);
    }

    @GetMapping("/series/{id}/valoraciones")
    public List<DValoracionResumen> obtenerValoracionesSerie(@PathVariable Long id) {
        return servicioValoracion.obtenerValoraciones(id, TipoContenido.SERIE).stream()
                .map(mapeador::dto)
                .toList();
    }

    @GetMapping("/series/{id}/valoraciones/media")
    public DMediaValoracion obtenerMediaSerie(@PathVariable Long id) {
        Double media = servicioValoracion.obtenerMedia(id, TipoContenido.SERIE);
        long total = servicioValoracion.obtenerTotal(id, TipoContenido.SERIE);
        return mapeador.dtoMedia(media, total);
    }
}
