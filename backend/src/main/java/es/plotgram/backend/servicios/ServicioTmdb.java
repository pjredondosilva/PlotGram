package es.plotgram.backend.servicios;
import es.plotgram.backend.rest.dto.tmdb.DPeliculaListado;
import es.plotgram.backend.rest.dto.tmdb.DSerieListado;
import es.plotgram.backend.rest.dto.tmdb.MapeadorTmdb;
import es.plotgram.backend.tmdb.dto.DRespuestaBusquedaPeliculasTmdb;
import es.plotgram.backend.tmdb.dto.DRespuestaBusquedaSeriesTmdb;
import es.plotgram.backend.tmdb.dto.DRespuestaGenerosTmdb;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import es.plotgram.backend.tmdb.dto.DGeneroTmdb;

@Service
public class ServicioTmdb {

    private final WebClient tmdb;
    private final String lang;
    private final MapeadorTmdb mapeador;

    private volatile Map<Integer, String> cacheGenerosPeliculas = Map.of();
    private volatile Map<Integer, String> cacheGenerosSeries = Map.of();

    private final Object lockGenerosPeliculas = new Object();
    private final Object lockGenerosSeries = new Object();

    public ServicioTmdb(WebClient tmdbWebClient,
                        @Value("${tmdb.lang:es-ES}") String lang,
                        MapeadorTmdb mapeador) {
        this.tmdb = tmdbWebClient;
        this.lang = lang;
        this.mapeador = mapeador;
    }

    public List<DPeliculaListado> buscarPeliculas(String consulta, int pagina) {
        DRespuestaBusquedaPeliculasTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/search/movie")
                        .queryParam("query", consulta)
                        .queryParam("language", lang)
                        .queryParam("page", pagina)
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

    public List<DSerieListado> buscarSeries(String consulta, int pagina) {
        DRespuestaBusquedaSeriesTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/search/tv")
                        .queryParam("query", consulta)
                        .queryParam("language", lang)
                        .queryParam("page", pagina)
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

    public List<DPeliculaListado> taquillaPeliculas(int pagina) {
        DRespuestaBusquedaPeliculasTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/movie/now_playing")
                        .queryParam("language", lang)
                        .queryParam("page", pagina)
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

    public List<DSerieListado> seriesDelMomento(int pagina) {
        DRespuestaBusquedaSeriesTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/trending/tv/week")
                        .queryParam("language", lang)
                        .queryParam("page", pagina)
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

    @PostConstruct
    public void precargarGeneros() {
        refrescarGeneros();
    }

    @Scheduled(cron = "0 0 4 1 * *", zone = "Europe/Madrid")
    public void refrescarGeneros() {
        try {
            Map<Integer, String> nuevos = ActualizarGenerosPeliculas();
            if (!nuevos.isEmpty()) {
                synchronized (lockGenerosPeliculas) {
                    cacheGenerosPeliculas = Map.copyOf(nuevos);
                }
            }
        } catch (Exception ignored) {
        }

        try {
            Map<Integer, String> nuevos = ActualizarGenerosSeries();
            if (!nuevos.isEmpty()) {
                synchronized (lockGenerosSeries) {
                    cacheGenerosSeries = Map.copyOf(nuevos);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private Map<Integer, String> generosPeliculas() {
        return cacheGenerosPeliculas;
    }

    private Map<Integer, String> generosSeries() {
        return cacheGenerosSeries;
    }

    private Map<Integer, String> ActualizarGenerosPeliculas() {
        DRespuestaGenerosTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/genre/movie/list")
                        .queryParam("language", lang)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaGenerosTmdb.class)
                .block();

        return (resp == null || resp.genres() == null)
                ? Map.of()
                : resp.genres().stream()
                .filter(g -> g.id() != null && g.name() != null)
                .collect(Collectors.toUnmodifiableMap(
                        DGeneroTmdb::id,
                        DGeneroTmdb::name,
                        (a, b) -> a
                ));
    }

    private Map<Integer, String> ActualizarGenerosSeries() {
        DRespuestaGenerosTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/genre/tv/list")
                        .queryParam("language", lang)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaGenerosTmdb.class)
                .block();

        return (resp == null || resp.genres() == null)
                ? Map.of()
                : resp.genres().stream()
                .filter(g -> g.id() != null && g.name() != null)
                .collect(Collectors.toUnmodifiableMap(
                        DGeneroTmdb::id,
                        DGeneroTmdb::name,
                        (a, b) -> a
                ));
    }

}
