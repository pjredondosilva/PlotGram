// ServicioListas.js
import { apiDelete, apiGet, apiPost, apiPut } from "./Api.js";

export function obtenerMisListas() {
    return apiGet("/api/usuarios/me/listas");
}

export function obtenerListasDeUsuario(idUsuario) {
    return apiGet(`/api/usuarios/${idUsuario}/listas`);
}

export function obtenerDetalleListaDeUsuario(idUsuario, idLista) {
    return apiGet(`/api/usuarios/${idUsuario}/listas/${idLista}`);
}

export function crearLista(dto) {
    return apiPost("/api/usuarios/me/listas", dto);
}

export function editarLista(idLista, dto) {
    return apiPut(`/api/usuarios/me/listas/${idLista}`, dto);
}

export function borrarLista(idLista) {
    return apiDelete(`/api/usuarios/me/listas/${idLista}`);
}

export function aniadirContenidoALista(idLista, dto) {
    return apiPost(`/api/usuarios/me/listas/${idLista}/elementos`, dto);
}

export function eliminarElementoDeLista(idLista, idElemento) {
    return apiDelete(`/api/usuarios/me/listas/${idLista}/elementos/${idElemento}`);
}