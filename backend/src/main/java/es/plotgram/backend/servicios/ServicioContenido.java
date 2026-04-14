package es.plotgram.backend.servicios;

import es.plotgram.backend.entidades.Contenido;
import es.plotgram.backend.entidades.TipoContenido;
import es.plotgram.backend.repositorios.RepositorioContenido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class ServicioContenido {

    private final RepositorioContenido repositorioContenido;

    public ServicioContenido(RepositorioContenido repositorioContenido) {
        this.repositorioContenido = repositorioContenido;
    }

    @Transactional(readOnly = true)
    public Optional<Contenido> buscarPorTmdbIdYTipo(Long tmdbId, TipoContenido tipo) {
        return repositorioContenido.buscarPorTmdbIdYTipo(tmdbId, tipo);
    }

    @Transactional
    public Contenido buscarOGuardar(Contenido contenido) {
        return repositorioContenido.buscarPorTmdbIdYTipo(contenido.getTmdbId(), contenido.getTipo())
                .orElseGet(() -> repositorioContenido.guardar(contenido));
    }
}