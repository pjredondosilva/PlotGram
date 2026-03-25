const BASE_URL = import.meta.env.VITE_API_BASE_URL;

async function handleResponse(res) {
    if (res.status === 204) return null;
    const contentType = res.headers.get("content-type") || "";
    const body = contentType.includes("application/json")
        ? await res.json().catch(() => null)
        : await res.text().catch(() => "");

    if (!res.ok) {
        let msg =
            typeof body === "string" ? body :
                body?.message ? body.message :
                    body?.fieldErrors ? Object.values(body.fieldErrors).join("\n") :
                        "";
        if (!msg && res.status === 401) {
            msg = "Nombre o contraseña incorrectos.";
        }
        if (!msg && res.status === 403) {
            msg = "No autorizado.";
        }
        const err = new Error(msg || `HTTP ${res.status}`);
        err.status = res.status;
        err.body = body;
        throw err;
    }

    return body;
}

export async function apiGet(path) {
    const res = await fetch(`${BASE_URL}${path}`, {
        method: "GET",
        credentials: "include",
        headers: { Accept: "application/json" },
    });
    return handleResponse(res);
}

export async function apiPost(path, data) {
    const res = await fetch(`${BASE_URL}${path}`, {
        method: "POST",
        credentials: "include",
        headers: {
            Accept: "application/json",
            ...(data !== undefined ? { "Content-Type": "application/json" } : {}),
        },
        ...(data !== undefined ? { body: JSON.stringify(data) } : {}),
    });
    return handleResponse(res);
}

export async function apiPut(path, data) {
    const res = await fetch(`${BASE_URL}${path}`, {
        method: "PUT",
        credentials: "include",
        headers: {
            Accept: "application/json",
            ...(data !== undefined ? { "Content-Type": "application/json" } : {}),
        },
        ...(data !== undefined ? { body: JSON.stringify(data) } : {}),
    });
    return handleResponse(res);
}

export async function apiDelete(path) {
    const res = await fetch(`${BASE_URL}${path}`, {
        method: "DELETE",
        credentials: "include",
        headers: { Accept: "application/json" },
    });
    return handleResponse(res);
}
