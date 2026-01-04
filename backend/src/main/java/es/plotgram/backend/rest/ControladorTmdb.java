package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.tmdb.DPeliculaListado;
import es.plotgram.backend.rest.dto.tmdb.DSerieListado;
import es.plotgram.backend.tmdb.servicios.ServicioTmdb;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ControladorTmdb {

    private final ServicioTmdb servicioTmdb;

    public ControladorTmdb(ServicioTmdb servicioTmdb) {
        this.servicioTmdb = servicioTmdb;
    }

    @GetMapping("/tmdb/peliculas")
    public List<DPeliculaListado> peliculas(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "1") int page
    ) {
        if (query == null || query.isBlank()) {
            return servicioTmdb.taquillaPeliculas(page);
        }
        return servicioTmdb.buscarPeliculas(query, page);
    }

    @GetMapping("/tmdb/series")
    public List<DSerieListado> series(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "1") int page
    ) {
        if (query == null || query.isBlank()) {
            return servicioTmdb.seriesDelMomento(page);
        }
        return servicioTmdb.buscarSeries(query, page);
    }

}

