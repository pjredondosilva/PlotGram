package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Contenido;
import es.plotgram.backend.entidades.TipoContenido;
import es.plotgram.backend.repositorios.RepositorioContenido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * Servicio encargado de la gestión y persistencia de los objetos de contenido.
 * Asegura que no se dupliquen contenidos con el mismo ID de TMDB.
 */
@Service
@Validated
public class ServicioContenido {

    private final RepositorioContenido repositorioContenido;

    public ServicioContenido(RepositorioContenido repositorioContenido) {
        this.repositorioContenido = repositorioContenido;
    }

    /**
     * Busca un contenido en la base de datos local o lo guarda si no existe.
     * 
     * @param contenido Contenido a buscar o persistir.
     * @return El contenido persistido con su ID generado.
     */
    @Transactional
    public Contenido buscarOGuardar(Contenido contenido) {
        return repositorioContenido.buscarPorTmdbIdYTipo(contenido.getTmdbId(), contenido.getTipo())
                .orElseGet(() -> repositorioContenido.guardar(contenido));
    }
}