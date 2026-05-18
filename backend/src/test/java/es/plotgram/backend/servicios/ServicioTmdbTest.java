package es.plotgram.backend.servicios;

import es.plotgram.backend.rest.dto.tmdb.*;
import es.plotgram.backend.tmdb.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioTmdbTest {

    @Mock
    private ExchangeFunction exchangeFunction;

    @Mock
    private MapeadorTmdb mapeador;

    private ServicioTmdb servicio;

    @BeforeEach
    void setUp() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost")
                .exchangeFunction(exchangeFunction)
                .build();

        servicio = new ServicioTmdb(webClient, "es-ES", mapeador);
    }

    @Test
    @DisplayName("listarPeliculas OK: si hay consulta, usa busqueda de Peliculas")
    void testListarPeliculasConConsulta() {
        mockRutas(Map.of(
                "/genre/movie/list", """
                        {"genres":[{"id":28,"name":"Accion"},{"id":878,"name":"Ciencia ficcion"}]}
                        """,
                "/genre/tv/list", """
                        {"genres":[]}
                        """,
                "/search/movie",
                """
                        {
                          "page": 2,
                          "total_pages": 5,
                          "total_results": 40,
                          "results": [
                            {"id": 1, "title": "Alien", "release_date": "1979-05-25", "poster_path": "/alien.jpg", "genre_ids": [28, 878]}
                          ]
                        }
                        """));
        when(mapeador.DtoPelicula(any(DPeliculaListadoRespuesta.class)))
                .thenReturn(new DPeliculaListado(1L, "Alien", "1979-05-25", "/alien.jpg", List.of(28, 878), List.of()));

        servicio.refrescarGeneros();
        var resultado = servicio.listarPeliculas("alien", 2, null, null, null);

        assertThat(resultado.page()).isEqualTo(2);
        assertThat(resultado.totalPages()).isEqualTo(5);
        assertThat(resultado.totalResults()).isEqualTo(40);
        assertThat(resultado.results()).containsExactly(
                new DPeliculaListado(1L, "Alien", "1979-05-25", "/alien.jpg", List.of(28, 878),
                        List.of("Accion", "Ciencia ficcion")));
        ClientRequest request = buscarPeticionPorRuta("/search/movie");
        assertThat(request.url().getQuery())
                .contains("query=alien")
                .contains("language=es-ES")
                .contains("page=2");
    }

    @Test
    @DisplayName("listarPeliculas OK: sin consulta ni filtros usa peliculas en taquilla")
    void testListarPeliculasSinConsultaNiFiltros() {
        mockRutas(Map.of(
                "/movie/now_playing",
                """
                        {
                          "page": 1,
                          "total_pages": 8,
                          "total_results": 100,
                          "results": [
                            {"id": 2, "title": "Dune", "release_date": "2024-03-01", "poster_path": "/dune.jpg", "genre_ids": [878]}
                          ]
                        }
                        """));
        when(mapeador.DtoPelicula(any(DPeliculaListadoRespuesta.class)))
                .thenReturn(new DPeliculaListado(2L, "Dune", "2024-03-01", "/dune.jpg", List.of(878), List.of()));

        var resultado = servicio.listarPeliculas(null, 1, null, null, null);

        assertThat(resultado.page()).isEqualTo(1);
        assertThat(resultado.results()).containsExactly(
                new DPeliculaListado(2L, "Dune", "2024-03-01", "/dune.jpg", List.of(878), List.of()));
        ClientRequest request = buscarPeticionPorRuta("/movie/now_playing");
        assertThat(request.url().getQuery())
                .contains("language=es-ES")
                .contains("page=1");
    }

    @Test
    @DisplayName("listarPeliculas OK: si hay filtros usa discover/movie con fechas y generos")
    void testListarPeliculasConFiltros() {
        mockRutas(Map.of(
                "/genre/movie/list", """
                        {"genres":[{"id":28,"name":"Accion"},{"id":18,"name":"Drama"}]}
                        """,
                "/genre/tv/list", """
                        {"genres":[]}
                        """,
                "/discover/movie",
                """
                        {
                          "page": 3,
                          "total_pages": 4,
                          "total_results": 20,
                          "results": [
                            {"id": 3, "title": "Mad Max", "release_date": "2015-05-15", "poster_path": "/madmax.jpg", "genre_ids": [28]}
                          ]
                        }
                        """));
        when(mapeador.DtoPelicula(any(DPeliculaListadoRespuesta.class)))
                .thenReturn(new DPeliculaListado(3L, "Mad Max", "2015-05-15", "/madmax.jpg", List.of(28), List.of()));

        servicio.refrescarGeneros();
        var resultado = servicio.listarPeliculas(" ", 3, "2015-01-01", "2015-12-31", List.of(" Accion "));

        assertThat(resultado.page()).isEqualTo(3);
        assertThat(resultado.results()).containsExactly(
                new DPeliculaListado(3L, "Mad Max", "2015-05-15", "/madmax.jpg", List.of(28), List.of("Accion")));
        ClientRequest request = buscarPeticionPorRuta("/discover/movie");
        assertThat(request.url().getQuery())
                .contains("primary_release_date.gte=2015-01-01")
                .contains("primary_release_date.lte=2015-12-31")
                .contains("with_genres=28")
                .contains("sort_by=primary_release_date.desc");
    }

    @Test
    @DisplayName("listarPeliculas KO: si se filtra por un genero inexistente, devuelve respuesta vacia sin llamar a discover")
    void testListarPeliculasGeneroInexistente() {
        mockRutas(Map.of(
                "/genre/movie/list", """
                        {"genres":[{"id":28,"name":"Accion"}]}
                        """,
                "/genre/tv/list", """
                        {"genres":[]}
                        """));

        servicio.refrescarGeneros();
        var resultado = servicio.listarPeliculas(null, 1, null, null, List.of("Musical inventado"));

        assertThat(resultado.page()).isEqualTo(1);
        assertThat(resultado.totalPages()).isEqualTo(1);
        assertThat(resultado.totalResults()).isZero();
        assertThat(resultado.results()).isEmpty();
        verify(mapeador, never()).DtoPelicula(any());
        assertThat(rutasSolicitadas()).doesNotContain("/discover/movie");
    }

    @Test
    @DisplayName("listarSeries OK: si hay consulta, usa busqueda de series")
    void testListarSeriesConConsulta() {
        mockRutas(Map.of(
                "/genre/movie/list", """
                        {"genres":[]}
                        """,
                "/genre/tv/list", """
                        {"genres":[{"id":18,"name":"Drama"},{"id":9648,"name":"Misterio"}]}
                        """,
                "/search/tv",
                """
                        {
                          "page": 2,
                          "total_pages": 4,
                          "total_results": 60,
                          "results": [
                            {"id": 4, "name": "Dark", "first_air_date": "2017-12-01", "poster_path": "/dark.jpg", "genre_ids": [18, 9648]}
                          ]
                        }
                        """));
        when(mapeador.DtoSerie(any(DSerieListadoRespuesta.class)))
                .thenReturn(new DSerieListado(4L, "Dark", "2017-12-01", "/dark.jpg", List.of(18, 9648), List.of()));

        servicio.refrescarGeneros();
        var resultado = servicio.listarSeries("dark", 2, null, null, null);

        assertThat(resultado.page()).isEqualTo(2);
        assertThat(resultado.results()).containsExactly(
                new DSerieListado(4L, "Dark", "2017-12-01", "/dark.jpg", List.of(18, 9648),
                        List.of("Drama", "Misterio")));
        ClientRequest request = buscarPeticionPorRuta("/search/tv");
        assertThat(request.url().getQuery()).contains("query=dark").contains("page=2");
    }

    @Test
    @DisplayName("listarSeries OK: si hay filtros usa discover/tv con fechas y generos")
    void testListarSeriesConFiltros() {
        mockRutas(Map.of(
                "/genre/movie/list", """
                        {"genres":[]}
                        """,
                "/genre/tv/list", """
                        {"genres":[{"id":10765,"name":"Sci-Fi & Fantasy"}]}
                        """,
                "/discover/tv",
                """
                        {
                          "page": 4,
                          "total_pages": 5,
                          "total_results": 50,
                          "results": [
                            {"id": 5, "name": "The Last of Us", "first_air_date": "2023-01-15", "poster_path": "/tlou.jpg", "genre_ids": [10765]}
                          ]
                        }
                        """));
        when(mapeador.DtoSerie(any(DSerieListadoRespuesta.class)))
                .thenReturn(
                        new DSerieListado(5L, "The Last of Us", "2023-01-15", "/tlou.jpg", List.of(10765), List.of()));

        servicio.refrescarGeneros();
        var resultado = servicio.listarSeries(null, 4, "2023-01-01", "2023-12-31", List.of("sci-fi & fantasy"));

        assertThat(resultado.page()).isEqualTo(4);
        assertThat(resultado.results()).containsExactly(
                new DSerieListado(5L, "The Last of Us", "2023-01-15", "/tlou.jpg", List.of(10765),
                        List.of("Sci-Fi & Fantasy")));
        ClientRequest request = buscarPeticionPorRuta("/discover/tv");
        assertThat(request.url().getQuery())
                .contains("first_air_date.gte=2023-01-01")
                .contains("first_air_date.lte=2023-12-31")
                .contains("with_genres=10765")
                .contains("sort_by=first_air_date.desc");
    }

    @Test
    @DisplayName("nombresGeneros OK: devuelve nombres distintos y ordenados")
    void testNombresGenerosOrdenados() {
        mockRutas(Map.of(
                "/genre/movie/list", """
                        {"genres":[{"id":28,"name":"Accion"},{"id":18,"name":"Drama"},{"id":12,"name":"Accion"}]}
                        """,
                "/genre/tv/list", """
                        {"genres":[{"id":35,"name":"Comedia"},{"id":9648,"name":"Misterio"}]}
                        """));

        servicio.refrescarGeneros();

        assertThat(servicio.nombresGenerosPeliculas()).containsExactly("Accion", "Drama");
        assertThat(servicio.nombresGenerosSeries()).containsExactly("Comedia", "Misterio");
    }

    @Test
    @DisplayName("detallePelicula OK: consulta TMDB y delega el mapeo del detalle")
    void testDetallePelicula() {
        mockRutas(Map.of(
                "/movie/10", """
                        {"id":10,"title":"Interstellar","overview":"Overview"}
                        """));
        var detalle = new DPeliculaDetalle(10L, "Interstellar", "Overview", null, null, null, null, null, List.of(),
                List.of(), null, new DProveedoresPelicula(List.of(), List.of(), List.of()), List.of(), null, null, null,
                null, "No disponible", false);
        when(mapeador.DtoPeliculaDetalle(any(DPeliculaDetalleRespuesta.class))).thenReturn(detalle);

        var resultado = servicio.detallePelicula(10L);

        assertThat(resultado).isSameAs(detalle);
        ClientRequest request = buscarPeticionPorRuta("/movie/10");
        assertThat(request.url().getQuery()).contains("append_to_response=").contains("credits")
                .contains("release_dates");
    }

    @Test
    @DisplayName("detalleSerie OK: consulta TMDB y delega el mapeo del detalle")
    void testDetalleSerie() {
        mockRutas(Map.of(
                "/tv/20", """
                        {"id":20,"name":"Dark","overview":"Overview"}
                        """));
        var detalle = new DSerieDetalle(20L, "Dark", "Overview", null, null, null, null, null, null, List.of(),
                List.of(), List.of(), "No disponible", "No disponible", "No disponible",
                new DProveedoresPelicula(List.of(), List.of(), List.of()), List.of());
        when(mapeador.DtoSerieDetalle(any(DSerieDetalleRespuesta.class))).thenReturn(detalle);

        var resultado = servicio.detalleSerie(20L);

        assertThat(resultado).isSameAs(detalle);
        ClientRequest request = buscarPeticionPorRuta("/tv/20");
        assertThat(request.url().getQuery()).contains("append_to_response=").contains("credits")
                .contains("recommendations");
    }

    @Test
    @DisplayName("detalleTemporada OK: consulta TMDB y delega el mapeo del detalle")
    void testDetalleTemporada() {
        mockRutas(Map.of(
                "/tv/30/season/2", """
                        {"id":300,"name":"Temporada 2","season_number":2,"episodes":[]}
                        """));
        var detalle = new DTemporadaDetalle(300L, "Temporada 2", null, 2, 0, null, null, List.of());
        when(mapeador.DtoTemporadaDetalle(any(DTemporadaDetalleRespuesta.class))).thenReturn(detalle);

        var resultado = servicio.detalleTemporada(30L, 2);

        assertThat(resultado).isSameAs(detalle);
        ClientRequest request = buscarPeticionPorRuta("/tv/30/season/2");
        assertThat(request.url().getQuery()).contains("language=es-ES");
    }

    @Test
    @DisplayName("detalleEpisodio OK: consulta TMDB y delega el mapeo del detalle")
    void testDetalleEpisodio() {
        mockRutas(Map.of(
                "/tv/40/season/3/episode/4", """
                        {"id":400,"name":"Episodio 4","episode_number":4,"season_number":3}
                        """));
        var detalle = new DEpisodioDetalle(400L, "Episodio 4", null, 4, 3, null, null, null, null, List.of());
        when(mapeador.DtoEpisodioDetalle(any(DEpisodioDetalleRespuesta.class))).thenReturn(detalle);

        var resultado = servicio.detalleEpisodio(40L, 3, 4);

        assertThat(resultado).isSameAs(detalle);
        ClientRequest request = buscarPeticionPorRuta("/tv/40/season/3/episode/4");
        assertThat(request.url().getQuery()).contains("append_to_response=").contains("credits");
    }

    @Test
    @DisplayName("refrescarGeneros KO: si una llamada falla, no lanza excepcion")
    void testRefrescarGenerosNoRompeSiTmdbFalla() {
        when(exchangeFunction.exchange(any())).thenAnswer(invocation -> {
            ClientRequest request = invocation.getArgument(0);
            String path = request.url().getPath();

            if (path.equals("/genre/movie/list")) {
                return Mono.error(new RuntimeException("Fallo TMDB"));
            }

            if (path.equals("/genre/tv/list")) {
                return respuestaJson("""
                        {"genres":[{"id":18,"name":"Drama"}]}
                        """);
            }

            return Mono.error(new IllegalStateException("Ruta no mockeada: " + path));
        });

        assertThatCode(() -> servicio.refrescarGeneros())
                .doesNotThrowAnyException();
        assertThat(servicio.nombresGenerosSeries()).containsExactly("Drama");
    }

    private void mockRutas(Map<String, String> respuestasPorRuta) {
        when(exchangeFunction.exchange(any())).thenAnswer(invocation -> {
            ClientRequest request = invocation.getArgument(0);
            String path = request.url().getPath();

            String body = respuestasPorRuta.get(path);
            if (body == null) {
                return Mono.error(new IllegalStateException("Ruta no mockeada: " + path));
            }

            return respuestaJson(body);
        });
    }

    private Mono<ClientResponse> respuestaJson(String body) {
        return Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(body)
                        .build());
    }

    private ClientRequest buscarPeticionPorRuta(String ruta) {
        return peticionesCapturadas().stream()
                .filter(request -> request.url().getPath().equals(ruta))
                .findFirst()
                .orElseThrow();
    }

    private List<String> rutasSolicitadas() {
        return peticionesCapturadas().stream()
                .map(request -> request.url().getPath())
                .toList();
    }

    private List<ClientRequest> peticionesCapturadas() {
        ArgumentCaptor<ClientRequest> captor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeFunction, atLeastOnce()).exchange(captor.capture());
        return captor.getAllValues();
    }
}
