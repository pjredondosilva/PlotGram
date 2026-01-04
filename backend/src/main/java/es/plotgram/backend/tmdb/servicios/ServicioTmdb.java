package es.plotgram.backend.tmdb.servicios;
import es.plotgram.backend.rest.dto.tmdb.DPeliculaListado;
import es.plotgram.backend.rest.dto.tmdb.DSerieListado;
import es.plotgram.backend.rest.dto.tmdb.MapeadorTmdb;
import es.plotgram.backend.tmdb.dto.DRespuestaBusquedaPeliculasTmdb;
import es.plotgram.backend.tmdb.dto.DRespuestaBusquedaSeriesTmdb;
import es.plotgram.backend.tmdb.dto.DRespuestaGenerosTmdb;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import es.plotgram.backend.tmdb.dto.DGeneroTmdb;
import es.plotgram.backend.tmdb.dto.DPeliculaListadoRespuesta;
import es.plotgram.backend.tmdb.dto.DSerieListadoRespuesta;

@Service
public class ServicioTmdb {

    private final WebClient tmdb;
    private final String lang;
    private final MapeadorTmdb mapeador;

    //Cache para los generos
    private static final Duration TTL_GENEROS = Duration.ofHours(24);

    private volatile Map<Integer, String> cacheGenerosPeliculas = Map.of();
    private volatile Instant cacheGenerosPeliculasAt = Instant.EPOCH;

    private volatile Map<Integer, String> cacheGenerosSeries = Map.of();
    private volatile Instant cacheGenerosSeriesAt = Instant.EPOCH;

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

        Map<Integer, String> mapa = generosPeliculas();
        return resp.results().stream()
                .map(mapeador::DtoPelicula)
                .map(dto -> conNombresDeGenero(dto, mapa))
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

        Map<Integer, String> mapa = generosSeries();
        return resp.results().stream()
                .map(mapeador::DtoSerie)
                .map(dto -> conNombresDeGenero(dto, mapa))
                .toList();

    }

    public List<DPeliculaListado> taquillaPeliculas(int page) {
        DRespuestaBusquedaPeliculasTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/movie/now_playing")
                        .queryParam("language", lang)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaBusquedaPeliculasTmdb.class)
                .block();

        if (resp == null || resp.results() == null) return List.of();
        Map<Integer, String> mapa = generosPeliculas();
        return resp.results().stream()
                .map(mapeador::DtoPelicula)
                .map(dto -> conNombresDeGenero(dto, mapa))
                .toList();

    }

    public List<DSerieListado> seriesDelMomento(int page) {
        DRespuestaBusquedaSeriesTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/trending/tv/week")
                        .queryParam("language", lang)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaBusquedaSeriesTmdb.class)
                .block();

        if (resp == null || resp.results() == null) return List.of();
        Map<Integer, String> mapa = generosSeries();
        return resp.results().stream()
                .map(mapeador::DtoSerie)
                .map(dto -> conNombresDeGenero(dto, mapa))
                .toList();

    }


    private DPeliculaListado conNombresDeGenero(DPeliculaListado dto, Map<Integer, String> mapa) {
        List<String> nombres = (dto.genreIds() == null ? List.<Integer>of() : dto.genreIds())
                .stream()
                .map(mapa::get)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return new DPeliculaListado(
                dto.id(),
                dto.title(),
                dto.releaseDate(),
                dto.posterPath(),
                dto.genreIds(),
                nombres
        );
    }

    private DSerieListado conNombresDeGenero(DSerieListado dto, Map<Integer, String> mapa) {
        List<String> nombres = (dto.genreIds() == null ? List.<Integer>of() : dto.genreIds())
                .stream()
                .map(mapa::get)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return new DSerieListado(
                dto.id(),
                dto.name(),
                dto.firstAirDate(),
                dto.posterPath(),
                dto.genreIds(),
                nombres
        );
    }

    private Map<Integer, String> generosPeliculas() {
        Instant now = Instant.now();
        if (!cacheGenerosPeliculas.isEmpty() && now.isBefore(cacheGenerosPeliculasAt.plus(TTL_GENEROS))) {
            return cacheGenerosPeliculas;
        }

        DRespuestaGenerosTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/genre/movie/list")
                        .queryParam("language", lang)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaGenerosTmdb.class)
                .block();

        Map<Integer, String> mapa = (resp == null || resp.genres() == null)
                ? Map.of()
                : resp.genres().stream()
                .filter(g -> g.id() != null && g.name() != null)
                .collect(Collectors.toUnmodifiableMap(DGeneroTmdb::id,DGeneroTmdb::name));

        cacheGenerosPeliculas = mapa;
        cacheGenerosPeliculasAt = now;
        return mapa;
    }

    private Map<Integer, String> generosSeries() {
        Instant now = Instant.now();
        if (!cacheGenerosSeries.isEmpty() && now.isBefore(cacheGenerosSeriesAt.plus(TTL_GENEROS))) {
            return cacheGenerosSeries;
        }

        DRespuestaGenerosTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/genre/tv/list")
                        .queryParam("language", lang)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaGenerosTmdb.class)
                .block();

        Map<Integer, String> mapa = (resp == null || resp.genres() == null)
                ? Map.of()
                : resp.genres().stream()
                .filter(g -> g.id() != null && g.name() != null)
                .collect(Collectors.toUnmodifiableMap(DGeneroTmdb::id,DGeneroTmdb::name));

        cacheGenerosSeries = mapa;
        cacheGenerosSeriesAt = now;
        return mapa;
    }


}
