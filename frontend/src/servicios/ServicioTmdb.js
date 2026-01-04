import { apiGet } from "./api";

export function getMovies(query, page = 1) {
    const q = (query ?? "").trim();
    const qs = q ? `?query=${encodeURIComponent(q)}&page=${page}` : `?page=${page}`;
    return apiGet(`/api/tmdb/peliculas${qs}`);
}

export function getSeries(query, page = 1) {
    const q = (query ?? "").trim();
    const qs = q ? `?query=${encodeURIComponent(q)}&page=${page}` : `?page=${page}`;
    return apiGet(`/api/tmdb/series${qs}`);
}