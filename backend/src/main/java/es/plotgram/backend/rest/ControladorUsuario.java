package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.DActualizacionPerfil;
import es.plotgram.backend.rest.dto.DUsuarioRegistro;
import es.plotgram.backend.rest.dto.DVerificacionContrasena;
import es.plotgram.backend.rest.dto.Dusuario;
import es.plotgram.backend.rest.dto.Mapeador;
import es.plotgram.backend.servicios.ServicioUsuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el registro y la consulta de usuarios.
 */
@RestController
@RequestMapping("/api")
public class ControladorUsuario {

    private final Mapeador mapeador;
    private final ServicioUsuario servicioUsuario;

    public ControladorUsuario(Mapeador mapeador, ServicioUsuario servicioUsuario) {
        this.mapeador = mapeador;
        this.servicioUsuario = servicioUsuario;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param dto datos necesarios para el registro
     * @return respuesta vacía con estado 201 si el registro se realiza correctamente
     */
    @PostMapping("/usuarios")
    public ResponseEntity<Void> nuevoUsuario(@Valid @RequestBody DUsuarioRegistro dto) {
        servicioUsuario.nuevoUsuario(mapeador.entidadNueva(dto));
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
        return servicioUsuario.buscarUsuario(id)
                .map(usuario -> ResponseEntity.ok(mapeador.dto(usuario)))
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
        return servicioUsuario.buscarUsuario(nombre)
                .map(usuario -> ResponseEntity.ok(mapeador.dto(usuario)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/usuarios/me/verificacioncontrasena")
    public ResponseEntity<Void> verificarContrasena(Authentication authentication,
                                                    @Valid @RequestBody DVerificacionContrasena dto) {
        servicioUsuario.verificarContrasenaActual(authentication.getName(), mapeador.contrasenaActual(dto));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/usuarios/me/actualizacionperfil")
    public ResponseEntity<Dusuario> actualizarPerfil(Authentication authentication,
                                                     @Valid @RequestBody DActualizacionPerfil dto) {
        Mapeador.DatosActualizacionPerfil datos = mapeador.datosActualizacionPerfil(dto);

        var usuario = servicioUsuario.actualizarPerfil(
                authentication.getName(),
                datos.nombre(),
                datos.email(),
                datos.fotoPerfil(),
                datos.descripcion(),
                datos.contrasenaActual(),
                datos.nuevaContrasena()
        );

        return ResponseEntity.ok(mapeador.dto(usuario));
    }
}
