package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.tmdb.DPeliculaListado;
import es.plotgram.backend.rest.dto.tmdb.DSerieListado;
import es.plotgram.backend.servicios.ServicioTmdb;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ControladorTmdb {

    private final ServicioTmdb servicioTmdb;

    public ControladorTmdb(ServicioTmdb servicioTmdb) {
        this.servicioTmdb = servicioTmdb;
    }

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

