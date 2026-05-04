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
 * Controlador REST encargado de la gestión de listas de reproducción personalizadas.
 * Permite a los usuarios crear, editar, eliminar y gestionar el contenido de sus colecciones.
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

    /**
     * Crea una nueva lista para el usuario autenticado.
     *
     * @param authentication Información de sesión del propietario de la lista.
     * @param dto DTO con los datos básicos de la nueva lista.
     * @return DTO con el detalle de la lista creada y estado 201 (Created).
     */
    @PostMapping("/usuarios/me/listas")
    public ResponseEntity<DListaDetalle> crearLista(Authentication authentication,
                                                    @Valid @RequestBody DListaNueva dto) {
        var detalle = servicioLista.crearLista(authentication.getName(), mapeador.entidadNueva(dto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }

    /**
     * Recupera el resumen de todas las listas pertenecientes al usuario autenticado.
     *
     * @param authentication Información de sesión.
     * @return Lista de resúmenes de las listas del usuario.
     */
    @GetMapping("/usuarios/me/listas")
    public ResponseEntity<List<DListaResumen>> obtenerMisListas(Authentication authentication) {
        List<DListaResumen> listas = servicioLista.obtenerMisListas(authentication.getName()).stream()
                .map(resumen -> mapeador.dtoResumen(resumen.lista(), resumen.totalElementos()))
                .toList();

        return ResponseEntity.ok(listas);
    }

    /**
     * Actualiza los metadatos (nombre, descripción, imagen) de una lista existente.
     *
     * @param idLista ID único de la lista a editar.
     * @param authentication Información de sesión.
     * @param dto DTO con los nuevos datos.
     * @return DTO con el detalle actualizado de la lista.
     */
    @PutMapping("/usuarios/me/listas/{idLista}")
    public ResponseEntity<DListaDetalle> editarMiLista(@PathVariable long idLista,
                                                       Authentication authentication,
                                                       @Valid @RequestBody DListaNueva dto) {
        var detalle = servicioLista.editarLista(idLista, authentication.getName(), mapeador.entidadNueva(dto));
        return ResponseEntity.ok(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }

    /**
     * Elimina definitivamente una lista y todos sus elementos asociados.
     *
     * @param idLista ID único de la lista a borrar.
     * @param authentication Información de sesión.
     * @return 204 (No Content) si la eliminación tiene éxito.
     */
    @DeleteMapping("/usuarios/me/listas/{idLista}")
    public ResponseEntity<Void> eliminarMiLista(@PathVariable long idLista,
                                                 Authentication authentication) {
        servicioLista.eliminarLista(idLista, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Añade una película o serie a una lista de reproducción existente.
     *
     * @param idLista ID único de la lista destino.
     * @param authentication Información de sesión.
     * @param dto DTO con los datos del contenido a añadir.
     * @return DTO con el detalle actualizado de la lista.
     */
    @PostMapping("/usuarios/me/listas/{idLista}/elementos")
    public ResponseEntity<DListaDetalle> aniadirElemento(@PathVariable long idLista,
                                                         Authentication authentication,
                                                         @Valid @RequestBody DContenidoListaNuevo dto) {
        var detalle = servicioLista.aniadirContenido(idLista, authentication.getName(), mapeador.entidadNueva(dto));
        return ResponseEntity.ok(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }

    /**
     * Elimina un elemento (película o serie) específico de una lista.
     *
     * @param idLista ID único de la lista.
     * @param idElemento ID único del elemento dentro de la lista.
     * @param authentication Información de sesión.
     * @return 204 (No Content) si el elemento se elimina correctamente.
     */
    @DeleteMapping("/usuarios/me/listas/{idLista}/elementos/{idElemento}")
    public ResponseEntity<Void> eliminarElemento(@PathVariable long idLista,
                                                  @PathVariable long idElemento,
                                                  Authentication authentication) {
        servicioLista.eliminarElemento(idLista, idElemento, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Recupera el resumen de las listas públicas de cualquier usuario por su ID.
     *
     * @param idUsuario ID del usuario cuyas listas se desean consultar.
     * @return Lista de resúmenes de sus listas.
     */
    @GetMapping("/usuarios/{idUsuario}/listas")
    public ResponseEntity<List<DListaResumen>> obtenerListasDeUsuario(@PathVariable long idUsuario) {
        List<DListaResumen> listas = servicioLista.obtenerListasDeUsuario(idUsuario).stream()
                .map(resumen -> mapeador.dtoResumen(resumen.lista(), resumen.totalElementos()))
                .toList();

        return ResponseEntity.ok(listas);
    }

    /**
     * Obtiene el detalle completo (incluyendo todos los elementos) de una lista específica.
     *
     * @param idUsuario ID del propietario de la lista.
     * @param idLista ID único de la lista a consultar.
     * @return DTO con el detalle completo de la lista.
     */
    @GetMapping("/usuarios/{idUsuario}/listas/{idLista}")
    public ResponseEntity<DListaDetalle> obtenerListaDeUsuario(@PathVariable long idUsuario,
                                                                @PathVariable long idLista) {
        var detalle = servicioLista.obtenerListaDeUsuario(idUsuario, idLista);
        return ResponseEntity.ok(mapeador.dtoDetalle(detalle.lista(), detalle.elementos()));
    }
}