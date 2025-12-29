package es.plotgram.backend.tmdb.servicios;
import es.plotgram.backend.rest.dto.tmdb.DPeliculaListado;
import es.plotgram.backend.rest.dto.tmdb.DSerieListado;
import es.plotgram.backend.rest.dto.tmdb.MapeadorTmdb;
import es.plotgram.backend.tmdb.dto.DRespuestaBusquedaPeliculasTmdb;
import es.plotgram.backend.tmdb.dto.DRespuestaBusquedaSeriesTmdb;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class ServicioTmdb {

    private final WebClient tmdb;
    private final String lang;
    private final MapeadorTmdb mapeador;

    public ServicioTmdb(WebClient tmdbWebClient,
                        @Value("${tmdb.lang:es-ES}") String lang,
                        MapeadorTmdb mapeador) {
        this.tmdb = tmdbWebClient;
        this.lang = lang;
        this.mapeador = mapeador;
    }

    public List<DPeliculaListado> buscarPeliculas(String query, int page) {
        DRespuestaBusquedaPeliculasTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/search/movie")
                        .queryParam("query", query)
                        .queryParam("language", lang)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaBusquedaPeliculasTmdb.class)
                .block();

        if (resp == null || resp.results() == null) return List.of();

        return resp.results().stream()
                .map(mapeador::Dto)
                .toList();
    }

    public List<DSerieListado> buscarSeries(String query, int page) {
        DRespuestaBusquedaSeriesTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/search/tv")
                        .queryParam("query", query)
                        .queryParam("language", lang)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaBusquedaSeriesTmdb.class)
                .block();

        if (resp == null || resp.results() == null) return List.of();

        return resp.results().stream()
                .map(mapeador::Dto)
                .toList();
    }
}
