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

    @GetMapping("/peliculas/generos")
    public List<String> darGenerosPeliculas() {
        return servicioTmdb.nombresGenerosPeliculas();
    }

    @GetMapping("/series/generos")
    public List<String> darGenerosSeries() {
        return servicioTmdb.nombresGenerosSeries();
    }

    @GetMapping("/peliculas/{id}")
    public DPeliculaDetalle darPeliculaPorId(@PathVariable long id) {
        return servicioTmdb.detallePelicula(id);
    }

    @GetMapping("/series/{id}")
    public DSerieDetalle darSeriePorId(@PathVariable long id) {
        return servicioTmdb.detalleSerie(id);
    }

    @GetMapping("/series/{id}/temporadas/{temporada}")
    public DTemporadaDetalle darTemporadaPorId(
            @PathVariable long id,
            @PathVariable int temporada
    ) {
        return servicioTmdb.detalleTemporada(id, temporada);
    }

    @GetMapping("/series/{id}/temporadas/{temporada}/episodios/{episodio}")
    public DEpisodioDetalle darEpisodioPorId(
            @PathVariable long id,
            @PathVariable int temporada,
            @PathVariable int episodio
    ) {
        return servicioTmdb.detalleEpisodio(id, temporada, episodio);
    }
}