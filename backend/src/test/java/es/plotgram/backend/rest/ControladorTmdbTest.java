package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.tmdb.*;
import es.plotgram.backend.servicios.ServicioTmdb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControladorTmdbTest {

    @Mock
    private ServicioTmdb servicioTmdb;

    private ControladorTmdb controlador;

    @BeforeEach
    void setUp() {
        controlador = new ControladorTmdb(servicioTmdb);
    }

    @Test
    @DisplayName("GET /api/peliculas delega en listarPeliculas con consulta, fechas, pagina y generos")
    void testDarPeliculas() {
        var respuestaEsperada = new DRespuestaPaginadaTmdb<>(
                2,
                5,
                40,
                List.of(new DPeliculaListado(10L, "Alien", "1979-05-25", "/alien.jpg", List.of(28, 878), List.of("AcciÃ³n", "Ciencia ficciÃ³n")))
        );
        when(servicioTmdb.listarPeliculas("alien", 2, "1979-01-01", "1980-01-01", List.of("AcciÃ³n")))
                .thenReturn(respuestaEsperada);

        var resultado = controlador.darPeliculas("alien", 2, "1979-01-01", "1980-01-01", List.of("AcciÃ³n"));

        assertThat(resultado).isSameAs(respuestaEsperada);
        verify(servicioTmdb).listarPeliculas("alien", 2, "1979-01-01", "1980-01-01", List.of("AcciÃ³n"));
    }

    @Test
    @DisplayName("GET /api/series delega en listarSeries con consulta, fechas, pagina y generos")
    void testDarSeries() {
        var respuestaEsperada = new DRespuestaPaginadaTmdb<>(
                3,
                6,
                60,
                List.of(new DSerieListado(20L, "Dark", "2017-12-01", "/dark.jpg", List.of(18, 9648), List.of("Drama", "Misterio")))
        );
        when(servicioTmdb.listarSeries("dark", 3, "2017-01-01", "2018-01-01", List.of("Drama")))
                .thenReturn(respuestaEsperada);

        var resultado = controlador.darSeries("dark", 3, "2017-01-01", "2018-01-01", List.of("Drama"));

        assertThat(resultado).isSameAs(respuestaEsperada);
        verify(servicioTmdb).listarSeries("dark", 3, "2017-01-01", "2018-01-01", List.of("Drama"));
    }

    @Test
    @DisplayName("GET /api/peliculas/generos delega en nombresGenerosPeliculas")
    void testDarGenerosPeliculas() {
        when(servicioTmdb.nombresGenerosPeliculas()).thenReturn(List.of("Accion", "Drama"));

        var resultado = controlador.darGenerosPeliculas();

        assertThat(resultado).containsExactly("Accion", "Drama");
        verify(servicioTmdb).nombresGenerosPeliculas();
    }

    @Test
    @DisplayName("GET /api/series/generos delega en nombresGenerosSeries")
    void testDarGenerosSeries() {
        when(servicioTmdb.nombresGenerosSeries()).thenReturn(List.of("Comedia", "Misterio"));

        var resultado = controlador.darGenerosSeries();

        assertThat(resultado).containsExactly("Comedia", "Misterio");
        verify(servicioTmdb).nombresGenerosSeries();
    }

    @Test
    @DisplayName("GET /api/peliculas/{id} delega en detallePelicula")
    void testDarPeliculaPorId() {
        var detalle = new DPeliculaDetalle(
                30L,
                "Interstellar",
                "Overview",
                "2014-11-07",
                "/poster.jpg",
                "/backdrop.jpg",
                169,
                8.7,
                List.of("Ciencia ficcion"),
                List.of(),
                new DTrailer("Trailer", "abc123", "YouTube"),
                new DProveedoresPelicula(List.of(), List.of(), List.of()),
                List.of(),
                "Christopher Nolan",
                "Jonathan Nolan",
                165000000L,
                700000000L,
                "Estados Unidos",
                true
        );
        when(servicioTmdb.detallePelicula(30L)).thenReturn(detalle);

        var resultado = controlador.darPeliculaPorId(30L);

        assertThat(resultado).isSameAs(detalle);
        verify(servicioTmdb).detallePelicula(30L);
    }

    @Test
    @DisplayName("GET /api/series/{id} delega en detalleSerie")
    void testDarSeriePorId() {
        var detalle = new DSerieDetalle(
                40L,
                "Breaking Bad",
                "Overview",
                "2008-01-20",
                "/poster.jpg",
                "/backdrop.jpg",
                5,
                62,
                9.0,
                List.of("Drama"),
                List.of(),
                List.of(),
                "Vince Gilligan",
                "Estados Unidos",
                "Finalizada",
                new DProveedoresPelicula(List.of(), List.of(), List.of()),
                List.of()
        );
        when(servicioTmdb.detalleSerie(40L)).thenReturn(detalle);

        var resultado = controlador.darSeriePorId(40L);

        assertThat(resultado).isSameAs(detalle);
        verify(servicioTmdb).detalleSerie(40L);
    }

    @Test
    @DisplayName("GET /api/series/{id}/temporadas/{temporada} delega en detalleTemporada")
    void testDarTemporadaPorId() {
        var detalle = new DTemporadaDetalle(50L, "Temporada 1", "Overview", 1, 8, "2020-01-01", "/poster.jpg", List.of());
        when(servicioTmdb.detalleTemporada(100L, 1)).thenReturn(detalle);

        var resultado = controlador.darTemporadaPorId(100L, 1);

        assertThat(resultado).isSameAs(detalle);
        verify(servicioTmdb).detalleTemporada(100L, 1);
    }

    @Test
    @DisplayName("GET /api/series/{id}/temporadas/{temporada}/episodios/{episodio} delega en detalleEpisodio")
    void testDarEpisodioPorId() {
        var detalle = new DEpisodioDetalle(60L, "Episodio 1", "Overview", 1, 1, "2020-01-01", "/still.jpg", 55, 8.2, List.of());
        when(servicioTmdb.detalleEpisodio(100L, 1, 1)).thenReturn(detalle);

        var resultado = controlador.darEpisodioPorId(100L, 1, 1);

        assertThat(resultado).isSameAs(detalle);
        verify(servicioTmdb).detalleEpisodio(100L, 1, 1);
    }
}
