package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.tmdb.*;
import es.plotgram.backend.servicios.ServicioTmdb;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el descubrimiento de contenidos (Películas y Series)
 * mediante la integración con la API externa de TMDB.
 */
@RestController
@RequestMapping("/api")
public class ControladorTmdb {

    private final ServicioTmdb servicioTmdb;

    public ControladorTmdb(ServicioTmdb servicioTmdb) {
        this.servicioTmdb = servicioTmdb;
    }

    /**
     * Endpoint para listar y buscar películas.
     * 
     * @param consulta Texto de búsqueda (opcional).
     * @param pagina Número de página (por defecto 1).
     * @param fechaDesde Filtro de fecha mínima.
     * @param fechaHasta Filtro de fecha máxima.
     * @param generos Lista de géneros para filtrar.
     * @return Respuesta paginada de películas.
     */
    @GetMapping("/peliculas")
    public DRespuestaPaginadaTmdb<DPeliculaListado> darPeliculas(
            @RequestParam(required = false) String consulta,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) List<String> generos
    ) {
        return servicioTmdb.listarPeliculas(consulta, pagina, fechaDesde, fechaHasta, generos);
    }

    /**
     * Endpoint para listar y buscar series.
     * 
     * @param consulta Texto de búsqueda (opcional).
     * @param pagina Número de página (por defecto 1).
     * @param fechaDesde Filtro de fecha mínima.
     * @param fechaHasta Filtro de fecha máxima.
     * @param generos Lista de géneros para filtrar.
     * @return Respuesta paginada de series.
     */
    @GetMapping("/series")
    public DRespuestaPaginadaTmdb<DSerieListado> darSeries(
            @RequestParam(required = false) String consulta,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) List<String> generos
    ) {
        return servicioTmdb.listarSeries(consulta, pagina, fechaDesde, fechaHasta, generos);
    }

    /**
     * Obtiene la lista de nombres de géneros para películas.
     * @return Lista de strings con los géneros.
     */
    @GetMapping("/peliculas/generos")
    public List<String> darGenerosPeliculas() {
        return servicioTmdb.nombresGenerosPeliculas();
    }

    /**
     * Obtiene la lista de nombres de géneros para series.
     * @return Lista de strings con los géneros.
     */
    @GetMapping("/series/generos")
    public List<String> darGenerosSeries() {
        return servicioTmdb.nombresGenerosSeries();
    }

    /**
     * Obtiene los detalles completos de una película por su ID de TMDB.
     * @param id Identificador de la película.
     * @return Objeto con el detalle de la película.
     */
    @GetMapping("/peliculas/{id}")
    public DPeliculaDetalle darPeliculaPorId(@PathVariable long id) {
        return servicioTmdb.detallePelicula(id);
    }

    /**
     * Obtiene los detalles completos de una serie por su ID de TMDB.
     * @param id Identificador de la serie.
     * @return Objeto con el detalle de la serie.
     */
    @GetMapping("/series/{id}")
    public DSerieDetalle darSeriePorId(@PathVariable long id) {
        return servicioTmdb.detalleSerie(id);
    }

    /**
     * Obtiene los detalles de una temporada específica de una serie.
     * @param id ID de la serie.
     * @param temporada Número de temporada.
     * @return Objeto con el detalle de la temporada.
     */
    @GetMapping("/series/{id}/temporadas/{temporada}")
    public DTemporadaDetalle darTemporadaPorId(
            @PathVariable long id,
            @PathVariable int temporada
    ) {
        return servicioTmdb.detalleTemporada(id, temporada);
    }

    /**
     * Obtiene los detalles de un episodio específico.
     * @param id ID de la serie.
     * @param temporada Número de temporada.
     * @param episodio Número de episodio.
     * @return Objeto con el detalle del episodio.
     */
    @GetMapping("/series/{id}/temporadas/{temporada}/episodios/{episodio}")
    public DEpisodioDetalle darEpisodioPorId(
            @PathVariable long id,
            @PathVariable int temporada,
            @PathVariable int episodio
    ) {
        return servicioTmdb.detalleEpisodio(id, temporada, episodio);
    }
}