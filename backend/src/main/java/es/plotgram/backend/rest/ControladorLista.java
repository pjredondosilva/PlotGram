package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.DContenidoListaNuevo;
import es.plotgram.backend.rest.dto.DListaDetalle;
import es.plotgram.backend.rest.dto.DListaNueva;
import es.plotgram.backend.rest.dto.DListaResumen;
import es.plotgram.backend.rest.dto.Mapeador;
import es.plotgram.backend.servicios.ServicioLista;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ControladorLista {

    private final ServicioLista servicioLista;
    private final Mapeador mapeador;

    public ControladorLista(ServicioLista servicioLista, Mapeador mapeador) {
        this.servicioLista = servicioLista;
        this.mapeador = mapeador;
    }

    @PostMapping("/listas")
    public ResponseEntity<DListaDetalle> crearLista(Authentication authentication,
                                                    @Valid @RequestBody DListaNueva dto) {
        var detalle = servicioLista.crearLista(authentication.getName(), mapeador.entidadNueva(dto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }

    @GetMapping("/listas/me")
    public ResponseEntity<List<DListaResumen>> obtenerMisListas(Authentication authentication) {
        List<DListaResumen> listas = servicioLista.obtenerListasDelUsuario(authentication.getName()).stream()
                .map(resumen -> mapeador.dtoResumen(resumen.lista(), resumen.totalElementos()))
                .toList();

        return ResponseEntity.ok(listas);
    }

    @GetMapping("/listas/{id}")
    public ResponseEntity<DListaDetalle> obtenerLista(@PathVariable long id,
                                                      Authentication authentication) {
        var detalle = servicioLista.obtenerLista(id, authentication.getName());
        return ResponseEntity.ok(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }

    @PostMapping("/listas/{id}/elementos")
    public ResponseEntity<DListaDetalle> aniadirElemento(@PathVariable long id,
                                                         Authentication authentication,
                                                         @Valid @RequestBody DContenidoListaNuevo dto) {
        var detalle = servicioLista.aniadirContenido(id, authentication.getName(), mapeador.entidadNueva(dto));
        return ResponseEntity.ok(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }

    @DeleteMapping("/listas/{id}/elementos/{idElemento}")
    public ResponseEntity<Void> eliminarElemento(@PathVariable long id,
                                                 @PathVariable long idElemento,
                                                 Authentication authentication) {
        servicioLista.eliminarElemento(id, idElemento, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/listas/{id}")
    public ResponseEntity<Void> eliminarLista(@PathVariable long id,
                                              Authentication authentication) {
        servicioLista.eliminarLista(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/listas/{id}")
    public ResponseEntity<DListaDetalle> editarLista(@PathVariable long id,
                                                     Authentication authentication,
                                                     @Valid @RequestBody DListaNueva dto) {
        var detalle = servicioLista.editarLista(id, authentication.getName(), mapeador.entidadNueva(dto));
        return ResponseEntity.ok(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }
}
