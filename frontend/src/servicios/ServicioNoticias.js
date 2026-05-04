import { apiGet } from "./api.js";

export async function obtenerNoticias(idioma = "", pagina = 1, tamano = 20) {
    let path = "/api/noticias";
    const params = new URLSearchParams();
    if (idioma) params.append("idioma", idioma);
    params.append("pagina", pagina);
    params.append("tamano", tamano);
    
    const queryString = params.toString();
    if (queryString) path += `?${queryString}`;
    
    return apiGet(path);
}
