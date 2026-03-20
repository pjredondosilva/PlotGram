package es.plotgram.backend.servicios;
import es.plotgram.backend.rest.dto.tmdb.*;
import es.plotgram.backend.tmdb.dto.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Servicio encargado de consultar información de películas y series en TMDB
 * y de enriquecer los resultados con los nombres de sus géneros.
 * <p>
 * Mantiene una caché en memoria con los géneros de películas y series para
 * evitar consultas repetidas al servicio externo.
 */
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

    /**
     * Busca películas en TMDB a partir de un texto de consulta.
     *
     * @param consulta texto de búsqueda
     * @param pagina número de página a consultar
     * @return listado de películas encontradas
     */
    public DRespuestaPaginadaTmdb<DPeliculaListado> buscarPeliculas(String consulta, int pagina) {
        DRespuestaBusquedaPeliculasTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/search/movie")
                        .queryParam("query", consulta)
                        .queryParam("language", lang)
                        .queryParam("page", pagina)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaBusquedaPeliculasTmdb.class)
                .block();

        if (resp == null || resp.results() == null) {
            return new DRespuestaPaginadaTmdb<>(1, 1, 0, List.of());
        }

        Map<Integer, String> mapa = generosPeliculas();
        List<DPeliculaListado> resultados = resp.results().stream()
                .map(mapeador::DtoPelicula)
                .map(dto -> conNombresDeGenero(dto, mapa))
                .toList();

        return new DRespuestaPaginadaTmdb<>(
                resp.page() != null ? resp.page() : 1,
                resp.totalPages() != null ? resp.totalPages() : 1,
                resp.totalResults() != null ? resp.totalResults() : 0,
                resultados
        );
    }

    /**
     * Busca series en TMDB a partir de un texto de consulta.
     *
     * @param consulta texto de búsqueda
     * @param pagina número de página a consultar
     * @return listado de series encontradas
     */
    public DRespuestaPaginadaTmdb<DSerieListado> buscarSeries(String consulta, int pagina) {
        DRespuestaBusquedaSeriesTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/search/tv")
                        .queryParam("query", consulta)
                        .queryParam("language", lang)
                        .queryParam("page", pagina)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaBusquedaSeriesTmdb.class)
                .block();

        if (resp == null || resp.results() == null) {
            return new DRespuestaPaginadaTmdb<>(1, 1, 0, List.of());
        }

        Map<Integer, String> mapa = generosSeries();
        List<DSerieListado> resultados = resp.results().stream()
                .map(mapeador::DtoSerie)
                .map(dto -> conNombresDeGenero(dto, mapa))
                .toList();

        return new DRespuestaPaginadaTmdb<>(
                resp.page() != null ? resp.page() : 1,
                resp.totalPages() != null ? resp.totalPages() : 1,
                resp.totalResults() != null ? resp.totalResults() : 0,
                resultados
        );
    }


    /**
     * Obtiene las películas en cartelera.
     *
     * @param pagina número de página a consultar
     * @return listado de películas en cartelera
     */
    public DRespuestaPaginadaTmdb<DPeliculaListado> taquillaPeliculas(int pagina) {
        DRespuestaBusquedaPeliculasTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/movie/now_playing")
                        .queryParam("language", lang)
                        .queryParam("page", pagina)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaBusquedaPeliculasTmdb.class)
                .block();

        if (resp == null || resp.results() == null) {
            return new DRespuestaPaginadaTmdb<>(1, 1, 0, List.of());
        }

        Map<Integer, String> mapa = generosPeliculas();
        List<DPeliculaListado> resultados = resp.results().stream()
                .map(mapeador::DtoPelicula)
                .map(dto -> conNombresDeGenero(dto, mapa))
                .toList();

        return new DRespuestaPaginadaTmdb<>(
                resp.page() != null ? resp.page() : 1,
                resp.totalPages() != null ? resp.totalPages() : 1,
                resp.totalResults() != null ? resp.totalResults() : 0,
                resultados
        );
    }


    /**
     * Obtiene las series del momento.
     *
     * @param pagina número de página a consultar
     * @return listado de series destacadas de la semana
     */
    public DRespuestaPaginadaTmdb<DSerieListado> seriesDelMomento(int pagina) {
        DRespuestaBusquedaSeriesTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/trending/tv/week")
                        .queryParam("language", lang)
                        .queryParam("page", pagina)
                        .build())
                .retrieve()
                .bodyToMono(DRespuestaBusquedaSeriesTmdb.class)
                .block();

        if (resp == null || resp.results() == null) {
            return new DRespuestaPaginadaTmdb<>(1, 1, 0, List.of());
        }

        Map<Integer, String> mapa = generosSeries();
        List<DSerieListado> resultados = resp.results().stream()
                .map(mapeador::DtoSerie)
                .map(dto -> conNombresDeGenero(dto, mapa))
                .toList();

        return new DRespuestaPaginadaTmdb<>(
                resp.page() != null ? resp.page() : 1,
                resp.totalPages() != null ? resp.totalPages() : 1,
                resp.totalResults() != null ? resp.totalResults() : 0,
                resultados
        );
    }

    /**
     * Sustituye los identificadores de género de una película por sus nombres
     * legibles usando la caché disponible.
     *
     * @param dto película con identificadores de género
     * @param mapa mapa de correspondencia entre id de género y nombre
     * @return una nueva película con la lista de nombres de género completada
     */
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

    /**
     * Sustituye los identificadores de género de una serie por sus nombres
     * legibles usando la caché disponible.
     *
     * @param dto serie con identificadores de género
     * @param mapa mapa de correspondencia entre id de género y nombre
     * @return una nueva serie con la lista de nombres de género completada
     */
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

    /**
     * Precarga la información de géneros al iniciar la aplicación.
     */
    @PostConstruct
    public void precargarGeneros() {
        refrescarGeneros();
    }

    /**
     * Actualiza la caché de géneros de películas y series.
     * Este proceso también se ejecuta de forma programada periódicamente.
     */
    @Scheduled(cron = "0 0 4 1 * *", zone = "Europe/Madrid")
    public void refrescarGeneros() {
        try {
            Map<Integer, String> nuevos = actualizarGenerosPeliculas();
            if (!nuevos.isEmpty()) {
                synchronized (lockGenerosPeliculas) {
                    cacheGenerosPeliculas = Map.copyOf(nuevos);
                }
            }
        } catch (Exception ignored) {
        }

        try {
            Map<Integer, String> nuevos = actualizarGenerosSeries();
            if (!nuevos.isEmpty()) {
                synchronized (lockGenerosSeries) {
                    cacheGenerosSeries = Map.copyOf(nuevos);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Devuelve la caché actual de géneros de películas.
     *
     * @return mapa de identificadores y nombres de géneros de películas
     */
    private Map<Integer, String> generosPeliculas() {
        return cacheGenerosPeliculas;
    }

    /**
     * Devuelve la caché actual de géneros de series.
     *
     * @return mapa de identificadores y nombres de géneros de series
     */
    private Map<Integer, String> generosSeries() {
        return cacheGenerosSeries;
    }

    /**
     * Consulta en TMDB el catálogo de géneros de películas y construye
     * un mapa inmutable con sus identificadores y nombres.
     *
     * @return mapa de géneros de películas, o vacío si la consulta falla
     *         o no devuelve resultados válidos
     */
    private Map<Integer, String> actualizarGenerosPeliculas() {
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

    /**
     * Consulta en TMDB el catálogo de géneros de series y construye
     * un mapa inmutable con sus identificadores y nombres.
     *
     * @return mapa de géneros de series, o vacío si la consulta falla
     *         o no devuelve resultados válidos
     */
    private Map<Integer, String> actualizarGenerosSeries() {
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


    public DPeliculaDetalle detallePelicula(long peliculaId) {
        var respuesta = tmdb.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{id}")
                        .queryParam("language", "es-ES")
                        .queryParam("append_to_response", "credits,videos,recommendations,watch/providers,release_dates")
                        .build(peliculaId))
                .retrieve()
                .bodyToMono(DPeliculaDetalleRespuesta.class)
                .block();

        if (respuesta == null) {
            throw new IllegalStateException("TMDB no devolvió datos para la película " + peliculaId);
        }

        return mapeador.DtoPeliculaDetalle(respuesta);
    }

    public DSerieDetalle detalleSerie(long serieId) {
        var respuesta = tmdb.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{id}")
                        .queryParam("language", "es-ES")
                        .queryParam("append_to_response", "credits")
                        .build(serieId))
                .retrieve()
                .bodyToMono(DSerieDetalleRespuesta.class)
                .block();

        if (respuesta == null) {
            throw new IllegalStateException("TMDB no devolvió datos para la serie " + serieId);
        }

        return mapeador.DtoSerieDetalle(respuesta);
    }

    public DTemporadaDetalle detalleTemporada(long serieId, int numeroTemporada) {
        var respuesta = tmdb.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{id}/season/{seasonNumber}")
                        .queryParam("language", "es-ES")
                        .build(serieId, numeroTemporada))
                .retrieve()
                .bodyToMono(DTemporadaDetalleRespuesta.class)
                .block();

        if (respuesta == null) {
            throw new IllegalStateException("TMDB no devolvió datos para la temporada " + numeroTemporada);
        }

        return mapeador.DtoTemporadaDetalle(respuesta);
    }

    public DEpisodioDetalle detalleEpisodio(long serieId, int numeroTemporada, int numeroEpisodio) {
        var respuesta = tmdb.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{id}/season/{seasonNumber}/episode/{episodeNumber}")
                        .queryParam("language", "es-ES")
                        .queryParam("append_to_response", "credits")
                        .build(serieId, numeroTemporada, numeroEpisodio))
                .retrieve()
                .bodyToMono(DEpisodioDetalleRespuesta.class)
                .block();

        if (respuesta == null) {
            throw new IllegalStateException("TMDB no devolvió datos para el episodio " + numeroEpisodio);
        }

        return mapeador.DtoEpisodioDetalle(respuesta);
    }
}
