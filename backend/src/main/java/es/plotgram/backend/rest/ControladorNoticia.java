package es.plotgram.backend.rest;

import es.plotgram.backend.app.DNoticia;
import es.plotgram.backend.servicios.ServicioNoticias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST encargado de exponer los endpoints relacionados con las noticias de cine.
 * Proporciona acceso a la caché de noticias con soporte para filtrado y paginación.
 */
@RestController
@RequestMapping("/api/noticias")
public class ControladorNoticia {

    @Autowired
    private ServicioNoticias servicioNoticias;

    /**
     * Obtiene una lista paginada de noticias, opcionalmente filtrada por idioma.
     * 
     * @param idioma Código de idioma opcional (es, us, etc.) para filtrar los resultados.
     * @param pagina Número de página a recuperar (por defecto 1).
     * @param tamano Cantidad de noticias por página (por defecto 20).
     * @return Lista de noticias que cumplen los criterios de filtrado y paginación.
     */
    @GetMapping
    public List<DNoticia> obtenerNoticias(
            @RequestParam(required = false) String idioma,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "20") int tamano
    ) {
        List<DNoticia> todas = servicioNoticias.getNoticias();

        // Aplicar filtro de idioma si se especifica
        List<DNoticia> filtradas = todas.stream()
                .filter(n -> idioma == null || n.idioma().equalsIgnoreCase(idioma))
                .collect(Collectors.toList());

        // Implementación de paginación manual sobre la caché
        int inicio = (pagina - 1) * tamano;
        if (inicio >= filtradas.size()) {
            return List.of();
        }

        int fin = Math.min(inicio + tamano, filtradas.size());
        return filtradas.subList(inicio, fin);
    }
}
