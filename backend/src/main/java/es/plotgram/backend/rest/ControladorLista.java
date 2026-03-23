package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.DContenidoListaNuevo;
import es.plotgram.backend.rest.dto.DListaDetalle;
import es.plotgram.backend.rest.dto.DListaNueva;
import es.plotgram.backend.rest.dto.DListaResumen;
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

    public ControladorLista(ServicioLista servicioLista) {
        this.servicioLista = servicioLista;
    }

    @PostMapping("/listas")
    public ResponseEntity<DListaDetalle> crearLista(Authentication authentication,
                                                    @Valid @RequestBody DListaNueva dto) {
        DListaDetalle creada = servicioLista.crearLista(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/listas/me")
    public ResponseEntity<List<DListaResumen>> obtenerMisListas(Authentication authentication) {
        return ResponseEntity.ok(servicioLista.obtenerListasDelUsuario(authentication.getName()));
    }

    @GetMapping("/listas/{id}")
    public ResponseEntity<DListaDetalle> obtenerLista(@PathVariable long id,
                                                      Authentication authentication) {
        return ResponseEntity.ok(servicioLista.obtenerLista(id, authentication.getName()));
    }

    @PostMapping("/listas/{id}/elementos")
    public ResponseEntity<DListaDetalle> aniadirElemento(@PathVariable long id,
                                                         Authentication authentication,
                                                         @Valid @RequestBody DContenidoListaNuevo dto) {
        return ResponseEntity.ok(servicioLista.aniadirContenido(id, authentication.getName(), dto));
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
}