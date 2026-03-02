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