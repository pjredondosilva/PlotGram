import { apiGet, apiPost } from "./Api.js";

export async function listarValoraciones(tipo, id) {
    const path = tipo === "movie" ? `/api/peliculas/${id}/valoraciones` : `/api/series/${id}/valoraciones`;
    return apiGet(path);
}

export async function obtenerMediaValoraciones(tipo, id) {
    const path = tipo === "movie" ? `/api/peliculas/${id}/valoraciones/media` : `/api/series/${id}/valoraciones/media`;
    return apiGet(path);
}

export async function valorarContenido(datos) {
    return apiPost("/api/valoraciones", datos);
}
