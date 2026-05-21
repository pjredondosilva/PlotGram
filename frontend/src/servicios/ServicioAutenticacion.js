import {apiDelete, apiGet, apiPost, apiPut} from "./Api.js";

export function registrarUsuario({ nombre, email, contrasenia }) {
    return apiPost("/api/usuarios", { nombre, email, contrasenia });
}

export function loginUsuario({ nombre, contrasenia }) {
    return apiPost("/api/sesiones", { nombre, contrasenia });
}
export async function logout() {
    return apiDelete("/api/sesiones/actual", {});
}

export async function getMe() {
    return apiGet("/api/usuarios/me");
}

export async function obtenerUsuario(id) {
    return apiGet(`/api/usuarios/${id}`);
}

export async function getEstadoSesion() {
    return apiGet("/api/sesiones/estado");
}

export async function RefrescarSesion() {
    return apiPost("/api/sesiones/renovacion");
}

export async function verificarContrasenaActual(contrasenaActual) {
    return apiPost("/api/usuarios/me/verificacioncontrasena", { contrasenaActual });
}

export async function actualizarMiPerfil(dto) {
    return apiPut("/api/usuarios/me/actualizacionperfil", dto);
}

export async function buscarUsuarios(query) {
    return apiGet(`/api/usuarios/busqueda?q=${encodeURIComponent(query)}`);
}

export async function seguirUsuario(id) {
    return apiPost(`/api/usuarios/${id}/seguidores`);
}

export async function dejarDeSeguirUsuario(id) {
    return apiDelete(`/api/usuarios/${id}/seguidores`);
}