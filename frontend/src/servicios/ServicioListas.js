import { apiDelete, apiGet, apiPost, apiPut } from "./api.js";

export function obtenerMisListas() {
    return apiGet("/api/listas/me");
}

export function obtenerDetalleLista(id) {
    return apiGet(`/api/listas/${id}`);
}

export function crearLista(dto) {
    return apiPost("/api/listas", dto);
}

export function editarLista(id, dto) {
    return apiPut(`/api/listas/${id}`, dto);
}

export function borrarLista(id) {
    return apiDelete(`/api/listas/${id}`);
}

export function aniadirContenidoALista(idLista, dto) {
    return apiPost(`/api/listas/${idLista}/elementos`, dto);
}