package es.plotgram.backend.rest.dto.tmdb;

import es.plotgram.backend.tmdb.dto.DPeliculaListadoRespuesta;
import es.plotgram.backend.tmdb.dto.DSerieListadoRespuesta;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapeadorTmdb {
    public DPeliculaListado DtoPelicula(DPeliculaListadoRespuesta m) {
        var ids = (m.genreIds() == null) ? List.<Integer>of() : m.genreIds();
        return new DPeliculaListado(
                m.id(),
                m.title(),
                m.releaseDate(),
                m.posterPath(),
                ids,
                List.of()
        );
    }

    public DSerieListado DtoSerie(DSerieListadoRespuesta t) {
        var ids = (t.genreIds() == null) ? List.<Integer>of() : t.genreIds();
        return new DSerieListado(
                t.id(),
                t.name(),
                t.firstAirDate(),
                t.posterPath(),
                ids,
                List.of()
        );
    }
}
