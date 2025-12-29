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

    @GetMapping("/peliculas/buscar")
    public List<DPeliculaListado> buscarPeliculas(@RequestParam String query,
                                                  @RequestParam(defaultValue = "1") int page) {
        return servicioTmdb.buscarPeliculas(query, page);
    }

    @GetMapping("/series/buscar")
    public List<DSerieListado> buscarSeries(@RequestParam String query,
                                            @RequestParam(defaultValue = "1") int page) {
        return servicioTmdb.buscarSeries(query, page);
    }
}

