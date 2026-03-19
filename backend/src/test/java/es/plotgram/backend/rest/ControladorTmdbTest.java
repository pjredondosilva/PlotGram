package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.tmdb.DPeliculaListado;
import es.plotgram.backend.rest.dto.tmdb.DRespuestaPaginadaTmdb;
import es.plotgram.backend.rest.dto.tmdb.DSerieListado;
import es.plotgram.backend.servicios.ServicioTmdb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ControladorTmdbTest {

    @Mock
    private ServicioTmdb servicioTmdb;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ControladorTmdb(servicioTmdb))
                .build();
    }

    @Test
    @DisplayName("GET /api/peliculas sin consulta llama a taquillaPeliculas y devuelve respuesta paginada")
    void testDarPeliculasSinConsulta() throws Exception {
        when(servicioTmdb.taquillaPeliculas(1)).thenReturn(
                new DRespuestaPaginadaTmdb<>(
                        1,
                        10,
                        200,
                        List.of(
                                new DPeliculaListado(
                                        1,
                                        "Alien",
                                        "1979-05-25",
                                        "/alien.jpg",
                                        List.of(28, 878),
                                        List.of("Acción", "Ciencia ficción")
                                )
                        )
                )
        );

        mockMvc.perform(get("/api/peliculas")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(10))
                .andExpect(jsonPath("$.totalResults").value(200))
                .andExpect(jsonPath("$.results[0].title").value("Alien"));

        verify(servicioTmdb).taquillaPeliculas(1);
        verify(servicioTmdb, never()).buscarPeliculas(anyString(), anyInt());
    }

    @Test
    @DisplayName("GET /api/peliculas con consulta llama a buscarPeliculas y devuelve respuesta paginada")
    void testDarPeliculasConConsulta() throws Exception {
        when(servicioTmdb.buscarPeliculas("alien", 2)).thenReturn(
                new DRespuestaPaginadaTmdb<>(
                        2,
                        5,
                        80,
                        List.of(
                                new DPeliculaListado(
                                        1,
                                        "Alien",
                                        "1979-05-25",
                                        "/alien.jpg",
                                        List.of(28, 878),
                                        List.of("Acción", "Ciencia ficción")
                                )
                        )
                )
        );

        mockMvc.perform(get("/api/peliculas")
                        .param("consulta", "alien")
                        .param("pagina", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.totalResults").value(80))
                .andExpect(jsonPath("$.results[0].title").value("Alien"));

        verify(servicioTmdb).buscarPeliculas("alien", 2);
        verify(servicioTmdb, never()).taquillaPeliculas(anyInt());
    }

    @Test
    @DisplayName("GET /api/peliculas con consulta en blanco llama a taquillaPeliculas")
    void testDarPeliculasConConsultaBlank() throws Exception {
        when(servicioTmdb.taquillaPeliculas(3)).thenReturn(
                new DRespuestaPaginadaTmdb<>(3, 10, 200, List.of())
        );

        mockMvc.perform(get("/api/peliculas")
                        .param("consulta", " ")
                        .param("pagina", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(3))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results").isEmpty());

        verify(servicioTmdb).taquillaPeliculas(3);
        verify(servicioTmdb, never()).buscarPeliculas(anyString(), anyInt());
    }

    @Test
    @DisplayName("GET /api/series sin consulta llama a seriesDelMomento y devuelve respuesta paginada")
    void testDarSeriesSinConsulta() throws Exception {
        when(servicioTmdb.seriesDelMomento(1)).thenReturn(
                new DRespuestaPaginadaTmdb<>(
                        1,
                        8,
                        160,
                        List.of(
                                new DSerieListado(
                                        10,
                                        "Dark",
                                        "2017-12-01",
                                        "/dark.jpg",
                                        List.of(18, 9648),
                                        List.of("Drama", "Misterio")
                                )
                        )
                )
        );

        mockMvc.perform(get("/api/series")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(8))
                .andExpect(jsonPath("$.totalResults").value(160))
                .andExpect(jsonPath("$.results[0].name").value("Dark"));

        verify(servicioTmdb).seriesDelMomento(1);
        verify(servicioTmdb, never()).buscarSeries(anyString(), anyInt());
    }

    @Test
    @DisplayName("GET /api/series con consulta llama a buscarSeries y devuelve respuesta paginada")
    void testDarSeriesConConsulta() throws Exception {
        when(servicioTmdb.buscarSeries("dark", 2)).thenReturn(
                new DRespuestaPaginadaTmdb<>(
                        2,
                        4,
                        60,
                        List.of(
                                new DSerieListado(
                                        10,
                                        "Dark",
                                        "2017-12-01",
                                        "/dark.jpg",
                                        List.of(18, 9648),
                                        List.of("Drama", "Misterio")
                                )
                        )
                )
        );

        mockMvc.perform(get("/api/series")
                        .param("consulta", "dark")
                        .param("pagina", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.totalResults").value(60))
                .andExpect(jsonPath("$.results[0].name").value("Dark"));

        verify(servicioTmdb).buscarSeries("dark", 2);
        verify(servicioTmdb, never()).seriesDelMomento(anyInt());
    }

    @Test
    @DisplayName("GET /api/series con consulta en blanco llama a seriesDelMomento")
    void testDarSeriesConConsultaBlank() throws Exception {
        when(servicioTmdb.seriesDelMomento(4)).thenReturn(
                new DRespuestaPaginadaTmdb<>(4, 8, 160, List.of())
        );

        mockMvc.perform(get("/api/series")
                        .param("consulta", " ")
                        .param("pagina", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(4))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results").isEmpty());

        verify(servicioTmdb).seriesDelMomento(4);
        verify(servicioTmdb, never()).buscarSeries(anyString(), anyInt());
    }
}