import { apiGet } from "./Api.js";

function construirQueryString(query, page = 1, filtros = {}) {
    const params = new URLSearchParams();
    const q = (query ?? "").trim();

    if (q) {
        params.set("consulta", q);
    }

    params.set("pagina", String(page));

    if (!q) {
        const fechaDesde = (filtros.fechaDesde ?? "").trim();
        const fechaHasta = (filtros.fechaHasta ?? "").trim();
        const generos = Array.isArray(filtros.generos) ? filtros.generos : [];

        if (fechaDesde) {
            params.set("fechaDesde", fechaDesde);
        }

        if (fechaHasta) {
            params.set("fechaHasta", fechaHasta);
        }

        generos
            .map((g) => (g ?? "").trim())
            .filter(Boolean)
            .forEach((genero) => params.append("generos", genero));
    }

    return params.toString();
}

export function getMovies(query, page = 1, filtros = {}) {
    const qs = construirQueryString(query, page, filtros);
    return apiGet(`/api/peliculas?${qs}`);
}

export function getSeries(query, page = 1, filtros = {}) {
    const qs = construirQueryString(query, page, filtros);
    return apiGet(`/api/series?${qs}`);
}

export function getMovieGenres() {
    return apiGet("/api/peliculas/generos");
}

export function getSeriesGenres() {
    return apiGet("/api/series/generos");
}

export function DarDetallesPeliculas(id) {
    return apiGet(`/api/peliculas/${id}`);
}

export function DarDetallesSeries(id) {
    return apiGet(`/api/series/${id}`);
}

export function DarDetallesTemporada(seriesId, seasonNumber) {
    return apiGet(`/api/series/${seriesId}/temporadas/${seasonNumber}`);
}

export function DarDetallesEpisodio(seriesId, seasonNumber, episodeNumber) {
    return apiGet(`/api/series/${seriesId}/temporadas/${seasonNumber}/episodios/${episodeNumber}`);
}