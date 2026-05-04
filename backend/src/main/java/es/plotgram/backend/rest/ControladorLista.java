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

/**
 * Controlador REST para la gestión de listas de reproducción personalizadas.
 * Permite a los usuarios crear, editar, eliminar y consultar colecciones de contenido.
 */
@RestController
@RequestMapping("/api")
public class ControladorLista {

    private final ServicioLista servicioLista;
    private final Mapeador mapeador;

    public ControladorLista(ServicioLista servicioLista, Mapeador mapeador) {
        this.servicioLista = servicioLista;
        this.mapeador = mapeador;
    }

    @PostMapping("/usuarios/me/listas")
    public ResponseEntity<DListaDetalle> crearLista(Authentication authentication,
                                                    @Valid @RequestBody DListaNueva dto) {
        var detalle = servicioLista.crearLista(authentication.getName(), mapeador.entidadNueva(dto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }

    @GetMapping("/usuarios/me/listas")
    public ResponseEntity<List<DListaResumen>> obtenerMisListas(Authentication authentication) {
        List<DListaResumen> listas = servicioLista.obtenerMisListas(authentication.getName()).stream()
                .map(resumen -> mapeador.dtoResumen(resumen.lista(), resumen.totalElementos()))
                .toList();

        return ResponseEntity.ok(listas);
    }

    @PutMapping("/usuarios/me/listas/{idLista}")
    public ResponseEntity<DListaDetalle> editarMiLista(@PathVariable long idLista,
                                                       Authentication authentication,
                                                       @Valid @RequestBody DListaNueva dto) {
        var detalle = servicioLista.editarLista(idLista, authentication.getName(), mapeador.entidadNueva(dto));
        return ResponseEntity.ok(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }

    @DeleteMapping("/usuarios/me/listas/{idLista}")
    public ResponseEntity<Void> eliminarMiLista(@PathVariable long idLista,
                                                Authentication authentication) {
        servicioLista.eliminarLista(idLista, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/usuarios/me/listas/{idLista}/elementos")
    public ResponseEntity<DListaDetalle> aniadirElemento(@PathVariable long idLista,
                                                         Authentication authentication,
                                                         @Valid @RequestBody DContenidoListaNuevo dto) {
        var detalle = servicioLista.aniadirContenido(idLista, authentication.getName(), mapeador.entidadNueva(dto));
        return ResponseEntity.ok(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }

    @DeleteMapping("/usuarios/me/listas/{idLista}/elementos/{idElemento}")
    public ResponseEntity<Void> eliminarElemento(@PathVariable long idLista,
                                                 @PathVariable long idElemento,
                                                 Authentication authentication) {
        servicioLista.eliminarElemento(idLista, idElemento, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuarios/{idUsuario}/listas")
    public ResponseEntity<List<DListaResumen>> obtenerListasDeUsuario(@PathVariable long idUsuario) {
        List<DListaResumen> listas = servicioLista.obtenerListasDeUsuario(idUsuario).stream()
                .map(resumen -> mapeador.dtoResumen(resumen.lista(), resumen.totalElementos()))
                .toList();

        return ResponseEntity.ok(listas);
    }

    @GetMapping("/usuarios/{idUsuario}/listas/{idLista}")
    public ResponseEntity<DListaDetalle> obtenerListaDeUsuario(@PathVariable long idUsuario,
                                                               @PathVariable long idLista) {
        var detalle = servicioLista.obtenerListaDeUsuario(idUsuario, idLista);
        return ResponseEntity.ok(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }
}