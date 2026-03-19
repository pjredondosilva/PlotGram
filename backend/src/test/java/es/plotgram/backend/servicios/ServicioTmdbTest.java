package es.plotgram.backend.servicios;

import es.plotgram.backend.rest.dto.tmdb.DPeliculaListado;
import es.plotgram.backend.rest.dto.tmdb.DSerieListado;
import es.plotgram.backend.rest.dto.tmdb.MapeadorTmdb;
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

import static org.assertj.core.api.Assertions.*;
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
    @DisplayName("buscarPeliculas OK: devuelve películas y resuelve nombres de géneros")
    void testBuscarPeliculasOk() {
        mockRutas(Map.of(
                "/genre/movie/list", """
                    {
                      "genres": [
                        {"id": 28, "name": "Acción"},
                        {"id": 878, "name": "Ciencia ficción"}
                      ]
                    }
                    """,
                "/genre/tv/list", """
                    {
                      "genres": []
                    }
                    """,
                "/search/movie", """
                    {
                      "results": [
                        {
                          "id": 1,
                          "title": "Alien",
                          "release_date": "1979-05-25",
                          "poster_path": "/alien.jpg",
                          "genre_ids": [28, 878]
                        }
                      ]
                    }
                    """
        ));

        when(mapeador.DtoPelicula(any()))
                .thenReturn(new DPeliculaListado(
                        1,
                        "Alien",
                        null,
                        null,
                        List.of(28, 878),
                        List.of()
                ));

        servicio.refrescarGeneros();
        List<DPeliculaListado> resultado = servicio.buscarPeliculas("alien", 1);

        assertThat(resultado).containsExactly(
                new DPeliculaListado(
                        1,
                        "Alien",
                        null,
                        null,
                        List.of(28, 878),
                        List.of("Acción", "Ciencia ficción")
                )
        );

        ArgumentCaptor<ClientRequest> captor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeFunction, atLeastOnce()).exchange(captor.capture());

        ClientRequest requestBusqueda = captor.getAllValues().stream()
                .filter(r -> r.url().getPath().equals("/search/movie"))
                .findFirst()
                .orElseThrow();

        assertThat(requestBusqueda.url().getQuery())
                .contains("query=alien")
                .contains("language=es-ES")
                .contains("page=1");
    }

    @Test
    @DisplayName("buscarPeliculas KO: si TMDB no devuelve resultados, retorna lista vacía")
    void testBuscarPeliculasSinResultados() {
        mockRutas(Map.of(
                "/search/movie", """
                    {
                    }
                    """
        ));

        List<DPeliculaListado> resultado = servicio.buscarPeliculas("nada", 1);

        assertThat(resultado).isEmpty();
        verifyNoInteractions(mapeador);
    }

    @Test
    @DisplayName("buscarSeries OK: devuelve series y resuelve nombres de géneros")
    void testBuscarSeriesOk() {
        mockRutas(Map.of(
                "/genre/movie/list", """
                    {
                      "genres": []
                    }
                    """,
                "/genre/tv/list", """
                    {
                      "genres": [
                        {"id": 18, "name": "Drama"},
                        {"id": 9648, "name": "Misterio"}
                      ]
                    }
                    """,
                "/search/tv", """
                    {
                      "results": [
                        {
                          "id": 2,
                          "name": "Dark",
                          "first_air_date": "2017-12-01",
                          "poster_path": "/dark.jpg",
                          "genre_ids": [18, 9648]
                        }
                      ]
                    }
                    """
        ));

        when(mapeador.DtoSerie(any()))
                .thenReturn(new DSerieListado(
                        2,
                        "Dark",
                        null,
                        null,
                        List.of(18, 9648),
                        List.of()
                ));

        servicio.refrescarGeneros();
        List<DSerieListado> resultado = servicio.buscarSeries("dark", 1);

        assertThat(resultado).containsExactly(
                new DSerieListado(
                        2,
                        "Dark",
                        null,
                        null,
                        List.of(18, 9648),
                        List.of("Drama", "Misterio")
                )
        );
    }

    @Test
    @DisplayName("taquillaPeliculas OK: devuelve películas en cartelera con nombres de géneros")
    void testTaquillaPeliculasOk() {
        mockRutas(Map.of(
                "/genre/movie/list", """
                    {
                      "genres": [
                        {"id": 28, "name": "Acción"}
                      ]
                    }
                    """,
                "/genre/tv/list", """
                    {
                      "genres": []
                    }
                    """,
                "/movie/now_playing", """
                    {
                      "results": [
                        {
                          "id": 10,
                          "title": "Dune",
                          "release_date": "2024-01-01",
                          "poster_path": "/dune.jpg",
                          "genre_ids": [28]
                        }
                      ]
                    }
                    """
        ));

        when(mapeador.DtoPelicula(any()))
                .thenReturn(new DPeliculaListado(
                        10,
                        "Dune",
                        null,
                        null,
                        List.of(28),
                        List.of()
                ));

        servicio.refrescarGeneros();
        List<DPeliculaListado> resultado = servicio.taquillaPeliculas(1);

        assertThat(resultado).containsExactly(
                new DPeliculaListado(
                        10,
                        "Dune",
                        null,
                        null,
                        List.of(28),
                        List.of("Acción")
                )
        );
    }

    @Test
    @DisplayName("seriesDelMomento OK: devuelve series trending con nombres de géneros")
    void testSeriesDelMomentoOk() {
        mockRutas(Map.of(
                "/genre/movie/list", """
                    {
                      "genres": []
                    }
                    """,
                "/genre/tv/list", """
                    {
                      "genres": [
                        {"id": 10765, "name": "Sci-Fi & Fantasy"}
                      ]
                    }
                    """,
                "/trending/tv/week", """
                    {
                      "results": [
                        {
                          "id": 20,
                          "name": "The Last of Us",
                          "first_air_date": "2023-01-15",
                          "poster_path": "/tlou.jpg",
                          "genre_ids": [10765]
                        }
                      ]
                    }
                    """
        ));

        when(mapeador.DtoSerie(any()))
                .thenReturn(new DSerieListado(
                        20,
                        "The Last of Us",
                        null,
                        null,
                        List.of(10765),
                        List.of()
                ));

        servicio.refrescarGeneros();
        List<DSerieListado> resultado = servicio.seriesDelMomento(1);

        assertThat(resultado).containsExactly(
                new DSerieListado(
                        20,
                        "The Last of Us",
                        null,
                        null,
                        List.of(10765),
                        List.of("Sci-Fi & Fantasy")
                )
        );
    }

    @Test
    @DisplayName("refrescarGeneros KO: si una llamada falla, no lanza excepción")
    void testRefrescarGenerosNoRompeSiTmdbFalla() {
        when(exchangeFunction.exchange(any())).thenAnswer(invocation -> {
            ClientRequest request = invocation.getArgument(0);
            String path = request.url().getPath();

            if (path.equals("/genre/movie/list")) {
                return Mono.error(new RuntimeException("Fallo TMDB"));
            }

            if (path.equals("/genre/tv/list")) {
                return respuestaJson("""
                    {
                      "genres": [
                        {"id": 18, "name": "Drama"}
                      ]
                    }
                    """);
            }

            return Mono.error(new IllegalStateException("Ruta no mockeada: " + path));
        });

        assertThatCode(() -> servicio.refrescarGeneros())
                .doesNotThrowAnyException();
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
                        .build()
        );
    }
}