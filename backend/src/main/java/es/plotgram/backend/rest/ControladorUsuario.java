package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.*;
import es.plotgram.backend.servicios.ServicioUsuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

/**
 * Controlador REST para el registro y la consulta de usuarios.
 */
@RestController
@RequestMapping("/api")
public class ControladorUsuario {
    @Autowired
    Mapeador mapeador;
    @Autowired
    ServicioUsuario serviciousuario;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param dto datos necesarios para el registro
     * @return respuesta vacía con estado 201 si el registro se realiza correctamente
     */
    @PostMapping("/usuarios")
    public ResponseEntity<Void> nuevoUsuario(@Valid @RequestBody DUsuarioRegistro dto) {
        serviciousuario.nuevoUsuario(mapeador.entidadNueva(dto));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Obtiene un usuario a partir de su identificador.
     *
     * @param id identificador del usuario
     * @return el usuario solicitado si existe; en caso contrario, respuesta 404
     */
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Dusuario> obtenerUsuario(@PathVariable long id) {
        return serviciousuario.buscarUsuario(id)
                .map(u -> ResponseEntity.ok(mapeador.dto(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Obtiene los datos del usuario autenticado.
     *
     * @param authentication información de autenticación de la petición actual
     * @return datos del usuario autenticado si existe; en caso contrario, respuesta 404
     */
    @GetMapping("/usuarios/me")
    public ResponseEntity<Dusuario> me(Authentication authentication) {
        String nombre = authentication.getName();
        return serviciousuario.buscarUsuario(nombre)
                .map(u -> ResponseEntity.ok(mapeador.dto(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    @PostMapping("/usuarios/me/verificacioncontrasena")
    public ResponseEntity<Void> verificarContrasena(Authentication authentication,
                                                    @Valid @RequestBody DVerificacionContrasena dto) {
        serviciousuario.verificarContrasenaActual(authentication.getName(), dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/usuarios/me/actualizacionperfil")
    public ResponseEntity<Dusuario> actualizarPerfil(Authentication authentication,
                                                     @Valid @RequestBody DActualizacionPerfil dto) {
        var usuario = serviciousuario.actualizarPerfil(authentication.getName(), dto);
        return ResponseEntity.ok(mapeador.dto(usuario));
    }
}
