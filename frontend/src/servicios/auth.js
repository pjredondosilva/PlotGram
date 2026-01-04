import { apiPost } from "./api";

export function registerUser({ nombre, email, contrasenia }) {
    return apiPost("/api/usuarios", { nombre, email, contrasenia });
}

export function loginUser({ nombre, contrasenia }) {
    return apiPost("/api/autenticacion", { nombre, contrasenia });
}
