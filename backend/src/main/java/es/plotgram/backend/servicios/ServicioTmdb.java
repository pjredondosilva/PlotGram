package es.plotgram.backend.servicios;

import es.plotgram.backend.rest.dto.tmdb.*;
import es.plotgram.backend.tmdb.dto.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la comunicación con la API externa de The Movie Database (TMDB).
 * Gestiona la búsqueda de contenidos, la recuperación de detalles, créditos, videos,
 * proveedores de streaming y la internacionalización de géneros.
 */
@Service
public class ServicioTmdb {

    private final WebClient tmdb;
    private final String lang;
    private final MapeadorTmdb mapeador;
    private static final String lenguaGeneros = "es-ES";

    private volatile Map<Integer, String> cacheGenerosPeliculas = Map.of();
    private volatile Map<Integer, String> cacheGenerosSeries = Map.of();

    private final Object bloqueoGenerosPeliculas = new Object();
    private final Object bloqueoGenerosSeries = new Object();

    public ServicioTmdb(WebClient tmdbWebClient,
                        @Value("${tmdb.lang:es-ES}") String lang,
                        MapeadorTmdb mapeador) {
        this.tmdb = tmdbWebClient;
        this.lang = lang;
        this.mapeador = mapeador;
    }

    /**
     * Lista películas permitiendo búsqueda por texto, filtros de fecha o géneros.
     * Si no hay filtros, devuelve las películas en cartelera.
     */
    public DRespuestaPaginadaTmdb<DPeliculaListado> listarPeliculas(String consulta,
                                                                    int pagina,
                                                                    String fechaDesde,
                                                                    String fechaHasta,
                                                                    List<String> generos) {
        boolean hayConsulta = consulta != null && !consulta.isBlank();
        boolean hayFiltros = hayFiltros(fechaDesde, fechaHasta, generos);

        if (hayConsulta) {
            return buscarPeliculas(consulta, pagina);
        }

        if (hayFiltros) {
            return descubrirPeliculasFiltradas(pagina, fechaDesde, fechaHasta, generos);
        }

        return taquillaPeliculas(pagina);
    }

    /**
     * Lista series permitiendo búsqueda por texto, filtros de fecha o géneros.
     * Si no hay filtros, devuelve las series del momento.
     */
    public DRespuestaPaginadaTmdb<DSerieListado> listarSeries(String consulta,
                                                              int pagina,
                                                              String fechaDesde,
                                                              String fechaHasta,
                                                              List<String> generos) {
        boolean hayConsulta = consulta != null && !consulta.isBlank();
        boolean hayFiltros = hayFiltros(fechaDesde, fechaHasta, generos);

        if (hayConsulta) {
            return buscarSeries(consulta, pagina);
        }

        if (hayFiltros) {
            return descubrirSeriesFiltradas(pagina, fechaDesde, fechaHasta, generos);
        }

        return seriesDelMomento(pagina);
    }

    /**
     * Busca películas en la API de TMDB mediante una consulta de texto.
     * 
     * @param consulta Texto a buscar.
     * @param pagina Número de página para los resultados.
     * @return Respuesta paginada con las películas encontradas.
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

        return mapearPeliculas(resp, generosPeliculas());
    }

    /**
     * Busca series en la API de TMDB mediante una consulta de texto.
     * 
     * @param consulta Texto a buscar.
     * @param pagina Número de página para los resultados.
     * @return Respuesta paginada con las series encontradas.
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

        return mapearSeries(resp, generosSeries());
    }

    /**
     * Obtiene las películas que se encuentran actualmente en cartelera.
     * 
     * @param pagina Número de página de resultados.
     * @return Lista paginada de películas en taquilla.
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

        return mapearPeliculas(resp, generosPeliculas());
    }

    /**
     * Obtiene las series que son tendencia durante la semana actual.
     * 
     * @param pagina Número de página de resultados.
     * @return Lista paginada de series populares.
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

        return mapearSeries(resp, generosSeries());
    }

    /**
     * Descubre películas aplicando filtros avanzados de fecha y género.
     * 
     * @param pagina Número de página de resultados.
     * @param fechaDesde Fecha mínima de lanzamiento.
     * @param fechaHasta Fecha máxima de lanzamiento.
     * @param generos Lista de nombres de géneros para filtrar.
     * @return Lista paginada de películas que cumplen los criterios.
     */
    public DRespuestaPaginadaTmdb<DPeliculaListado> descubrirPeliculasFiltradas(int pagina,
                                                                                String fechaDesde,
                                                                                String fechaHasta,
                                                                                List<String> generos) {
        Map<Integer, String> mapa = generosPeliculas();
        List<Integer> idsGenero = resolverIdsGenero(generos, mapa);

        if (hayGenerosSeleccionados(generos) && idsGenero.isEmpty()) {
            return new DRespuestaPaginadaTmdb<>(1, 1, 0, List.of());
        }

        DRespuestaBusquedaPeliculasTmdb resp = tmdb.get()
                .uri(uri -> {
                    var builder = uri.path("/discover/movie")
                            .queryParam("language", lang)
                            .queryParam("page", pagina)
                            .queryParam("sort_by", "primary_release_date.desc");

                    if (fechaValida(fechaDesde)) {
                        builder.queryParam("primary_release_date.gte", fechaDesde);
                    }
                    if (fechaValida(fechaHasta)) {
                        builder.queryParam("primary_release_date.lte", fechaHasta);
                    }
                    if (!idsGenero.isEmpty()) {
                        builder.queryParam("with_genres", idsGenero.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(",")));
                    }

                    return builder.build();
                })
                .retrieve()
                .bodyToMono(DRespuestaBusquedaPeliculasTmdb.class)
                .block();

        return mapearPeliculas(resp, mapa);
    }

    /**
     * Descubre series aplicando filtros avanzados de fecha y género.
     * 
     * @param pagina Número de página de resultados.
     * @param fechaDesde Fecha mínima de primera emisión.
     * @param fechaHasta Fecha máxima de primera emisión.
     * @param generos Lista de nombres de géneros para filtrar.
     * @return Lista paginada de series que cumplen los criterios.
     */
    public DRespuestaPaginadaTmdb<DSerieListado> descubrirSeriesFiltradas(int pagina,
                                                                          String fechaDesde,
                                                                          String fechaHasta,
                                                                          List<String> generos) {
        Map<Integer, String> mapa = generosSeries();
        List<Integer> idsGenero = resolverIdsGenero(generos, mapa);

        if (hayGenerosSeleccionados(generos) && idsGenero.isEmpty()) {
            return new DRespuestaPaginadaTmdb<>(1, 1, 0, List.of());
        }

        DRespuestaBusquedaSeriesTmdb resp = tmdb.get()
                .uri(uri -> {
                    var builder = uri.path("/discover/tv")
                            .queryParam("language", lang)
                            .queryParam("page", pagina)
                            .queryParam("sort_by", "first_air_date.desc");

                    if (fechaValida(fechaDesde)) {
                        builder.queryParam("first_air_date.gte", fechaDesde);
                    }
                    if (fechaValida(fechaHasta)) {
                        builder.queryParam("first_air_date.lte", fechaHasta);
                    }
                    if (!idsGenero.isEmpty()) {
                        builder.queryParam("with_genres", idsGenero.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(",")));
                    }

                    return builder.build();
                })
                .retrieve()
                .bodyToMono(DRespuestaBusquedaSeriesTmdb.class)
                .block();

        return mapearSeries(resp, mapa);
    }

    /**
     * Devuelve la lista de nombres de géneros de películas disponibles en la plataforma.
     * 
     * @return Lista de strings con los nombres de los géneros.
     */
    public List<String> nombresGenerosPeliculas() {
        return cacheGenerosPeliculas.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /**
     * Devuelve la lista de nombres de géneros de series disponibles en la plataforma.
     * 
     * @return Lista de strings con los nombres de los géneros.
     */
    public List<String> nombresGenerosSeries() {
        return cacheGenerosSeries.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private boolean hayFiltros(String fechaDesde, String fechaHasta, List<String> generos) {
        return fechaValida(fechaDesde) || fechaValida(fechaHasta) || hayGenerosSeleccionados(generos);
    }

    private boolean fechaValida(String fecha) {
        return fecha != null && !fecha.isBlank();
    }

    private boolean hayGenerosSeleccionados(List<String> generos) {
        return generos != null && generos.stream().anyMatch(g -> g != null && !g.isBlank());
    }

    private List<Integer> resolverIdsGenero(List<String> nombres, Map<Integer, String> mapa) {
        if (nombres == null || nombres.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> porNombre = mapa.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        e -> normalizarGenero(e.getValue()),
                        Map.Entry::getKey,
                        (a, b) -> a
                ));

        return nombres.stream()
                .filter(Objects::nonNull)
                .map(this::normalizarGenero)
                .filter(nombre -> !nombre.isBlank())
                .map(porNombre::get)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String normalizarGenero(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    private DRespuestaPaginadaTmdb<DPeliculaListado> mapearPeliculas(DRespuestaBusquedaPeliculasTmdb resp,
                                                                     Map<Integer, String> mapa) {
        if (resp == null || resp.results() == null) {
            return new DRespuestaPaginadaTmdb<>(1, 1, 0, List.of());
        }

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

    private DRespuestaPaginadaTmdb<DSerieListado> mapearSeries(DRespuestaBusquedaSeriesTmdb resp,
                                                               Map<Integer, String> mapa) {
        if (resp == null || resp.results() == null) {
            return new DRespuestaPaginadaTmdb<>(1, 1, 0, List.of());
        }

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

    /**
     * Tarea inicial que carga los géneros en la caché al arrancar el sistema.
     */
    @PostConstruct
    public void precargarGeneros() {
        refrescarGeneros();
    }

    /**
     * Tarea programada que refresca la caché de géneros el día 1 de cada mes.
     */
    @Scheduled(cron = "0 0 4 1 * *", zone = "Europe/Madrid")
    public void refrescarGeneros() {
        try {
            Map<Integer, String> nuevos = actualizarGenerosPeliculas();
            if (!nuevos.isEmpty()) {
                synchronized (bloqueoGenerosPeliculas) {
                    cacheGenerosPeliculas = Map.copyOf(nuevos);
                }
            }
        } catch (Exception ignored) {
        }

        try {
            Map<Integer, String> nuevos = actualizarGenerosSeries();
            if (!nuevos.isEmpty()) {
                synchronized (bloqueoGenerosSeries) {
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

    private Map<Integer, String> actualizarGenerosPeliculas() {
        DRespuestaGenerosTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/genre/movie/list")
                        .queryParam("language", lenguaGeneros)
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

    private Map<Integer, String> actualizarGenerosSeries() {
        DRespuestaGenerosTmdb resp = tmdb.get()
                .uri(uri -> uri.path("/genre/tv/list")
                        .queryParam("language", lenguaGeneros)
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
     * Obtiene el detalle completo de una película incluyendo créditos y proveedores.
     * 
     * @param peliculaId ID de la película en TMDB.
     */
    public DPeliculaDetalle detallePelicula(Long peliculaId) {
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

    /**
     * Obtiene el detalle completo de una serie.
     * 
     * @param serieId ID de la serie en TMDB.
     */
    public DSerieDetalle detalleSerie(Long serieId) {
        var respuesta = tmdb.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{id}")
                        .queryParam("language", "es-ES")
                        .queryParam("append_to_response", "credits,recommendations,watch/providers")
                        .build(serieId))
                .retrieve()
                .bodyToMono(DSerieDetalleRespuesta.class)
                .block();

        if (respuesta == null) {
            throw new IllegalStateException("TMDB no devolvió datos para la serie " + serieId);
        }

        return mapeador.DtoSerieDetalle(respuesta);
    }

    /**
     * Obtiene los detalles de una temporada específica de una serie.
     * 
     * @param serieId ID de la serie.
     * @param numeroTemporada Número de la temporada.
     * @return Objeto con los datos de la temporada.
     */
    public DTemporadaDetalle detalleTemporada(Long serieId, int numeroTemporada) {
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

    /**
     * Obtiene los detalles de un episodio específico de una serie.
     * 
     * @param serieId ID de la serie.
     * @param numeroTemporada Número de la temporada.
     * @param numeroEpisodio Número del episodio.
     * @return Objeto con los datos del episodio.
     */
    public DEpisodioDetalle detalleEpisodio(Long serieId, int numeroTemporada, int numeroEpisodio) {
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