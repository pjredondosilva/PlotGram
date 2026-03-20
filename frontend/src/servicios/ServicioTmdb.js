import { apiGet } from "./api.js";

export function getMovies(query, page = 1) {
    const q = (query ?? "").trim();
    const qs = q ? `?consulta=${encodeURIComponent(q)}&pagina=${page}` : `?pagina=${page}`;
    return apiGet(`/api/peliculas${qs}`);
}

export function getSeries(query, page = 1) {
    const q = (query ?? "").trim();
    const qs = q ? `?consulta=${encodeURIComponent(q)}&pagina=${page}` : `?pagina=${page}`;
    return apiGet(`/api/series${qs}`);
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
