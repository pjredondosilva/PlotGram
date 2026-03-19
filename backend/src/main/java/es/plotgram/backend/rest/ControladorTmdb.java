package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.tmdb.DPeliculaListado;
import es.plotgram.backend.rest.dto.tmdb.DSerieListado;
import es.plotgram.backend.servicios.ServicioTmdb;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la consulta de películas y series a través de TMDB.
 * Permite obtener listados generales o resultados filtrados por texto.
 */
@RestController
@RequestMapping("/api")
public class ControladorTmdb {

    private final ServicioTmdb servicioTmdb;

    public ControladorTmdb(ServicioTmdb servicioTmdb) {
        this.servicioTmdb = servicioTmdb;
    }

    /**
     * Devuelve un listado de películas.
     * Si no se indica una consulta, devuelve las películas en cartelera;
     * en caso contrario, realiza una búsqueda por texto.
     *
     * @param consulta texto de búsqueda opcional
     * @param pagina número de página a consultar
     * @return listado de películas obtenido desde TMDB
     */
    @GetMapping("/peliculas")
    public List<DPeliculaListado> DarPeliculas(
            @RequestParam(required = false) String consulta,
            @RequestParam(defaultValue = "1") int pagina
    ) {
        if (consulta == null || consulta.isBlank()) {
            return servicioTmdb.taquillaPeliculas(pagina);
        }
        return servicioTmdb.buscarPeliculas(consulta, pagina);
    }

    /**
     * Devuelve un listado de series.
     * Si no se indica una consulta, devuelve las series del momento;
     * en caso contrario, realiza una búsqueda por texto.
     *
     * @param consulta texto de búsqueda opcional
     * @param pagina número de página a consultar
     * @return listado de series obtenido desde TMDB
     */
    @GetMapping("/series")
    public List<DSerieListado> DarSeries(
            @RequestParam(required = false) String consulta,
            @RequestParam(defaultValue = "1") int pagina
    ) {
        if (consulta == null || consulta.isBlank()) {
            return servicioTmdb.seriesDelMomento(pagina);
        }
        return servicioTmdb.buscarSeries(consulta, pagina);
    }

}

