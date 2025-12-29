package es.plotgram.backend.rest.dto.tmdb;

import es.plotgram.backend.tmdb.dto.DPeliculaListadoRespuesta;
import es.plotgram.backend.tmdb.dto.DSerieListadoRespuesta;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapeadorTmdb {
    public DPeliculaListado Dto(DPeliculaListadoRespuesta m) {
        return new DPeliculaListado(
                m.id(),
                m.title(),
                m.releaseDate(),
                m.posterPath(),
                m.genreIds() == null ? List.of() : m.genreIds()
        );
    }

    public DSerieListado Dto(DSerieListadoRespuesta t) {
        return new DSerieListado(
                t.id(),
                t.name(),
                t.firstAirDate(),
                t.posterPath()
        );
    }
}
