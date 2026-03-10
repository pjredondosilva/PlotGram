import {apiDelete, apiGet, apiPost} from "./api.js";

export function registerUser({ nombre, email, contrasenia }) {
    return apiPost("/api/usuarios", { nombre, email, contrasenia });
}

export function loginUser({ nombre, contrasenia }) {
    return apiPost("/api/sesiones", { nombre, contrasenia });
}
export async function logout() {
    return apiDelete("/api/sesiones/actual", {});
}

export async function getMe() {
    return apiGet("/api/usuarios/me");
}

export async function getSessionState() {
    return apiGet("/api/sesiones/estado");
}

export async function refreshSession() {
    return apiPost("/api/sesiones/renovacion");
}