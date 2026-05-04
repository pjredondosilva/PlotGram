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

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios.
 * Maneja el registro, la recuperación de perfiles, la búsqueda y el sistema de seguidores.
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
     * @param dto Datos necesarios para el registro (nombre, email, contraseña).
     * @return Respuesta vacía con estado 201 (Created) si el registro tiene éxito.
     */
    @PostMapping("/usuarios")
    public ResponseEntity<Void> nuevoUsuario(@Valid @RequestBody DUsuarioRegistro dto) {
        servicioUsuario.nuevoUsuario(mapeador.entidadNueva(dto));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Obtiene la información pública de un usuario por su identificador.
     * Indica también si el usuario autenticado sigue al perfil consultado.
     *
     * @param id Identificador único del usuario.
     * @param authentication Información de autenticación del usuario actual.
     * @return El DTO del usuario solicitado o 404 si no existe.
     */
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Dusuario> obtenerUsuario(@PathVariable long id, Authentication authentication) {
        return servicioUsuario.buscarUsuario(id)
                .map(usuario -> {
                    Boolean loSigo = null;
                    if (authentication != null && authentication.isAuthenticated()) {
                        loSigo = servicioUsuario.esSeguidor(authentication.getName(), id);
                    }
                    return ResponseEntity.ok(mapeador.dto(usuario, loSigo));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Obtiene los datos del perfil privado del usuario autenticado.
     *
     * @param authentication Información de sesión del usuario.
     * @return El DTO con los datos del usuario actual.
     */
    @GetMapping("/usuarios/me")
    public ResponseEntity<Dusuario> me(Authentication authentication) {
        String nombre = authentication.getName();
        return servicioUsuario.buscarUsuario(nombre)
                .map(usuario -> ResponseEntity.ok(mapeador.dto(usuario)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Verifica si la contraseña actual proporcionada por el usuario es correcta.
     * Paso previo necesario para realizar cambios sensibles en el perfil.
     *
     * @param authentication Información de sesión.
     * @param dto DTO con la contraseña actual a verificar.
     * @return 204 (No Content) si es correcta.
     */
    @PostMapping("/usuarios/me/verificacioncontrasena")
    public ResponseEntity<Void> verificarContrasena(Authentication authentication,
                                                    @Valid @RequestBody DVerificacionContrasena dto) {
        servicioUsuario.verificarContrasenaActual(authentication.getName(), mapeador.contrasenaActual(dto));
        return ResponseEntity.noContent().build();
    }

    /**
     * Actualiza la información del perfil del usuario autenticado.
     *
     * @param authentication Información de sesión.
     * @param dto DTO con los nuevos datos del perfil.
     * @return El usuario actualizado.
     */
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

    /**
     * Busca usuarios cuyo nombre coincida parcialmente con el término de búsqueda.
     *
     * @param q Término de búsqueda.
     * @return Lista de usuarios que coinciden con el criterio.
     */
    @GetMapping("/usuarios/busqueda")
    public List<Dusuario> buscarUsuarios(@RequestParam String q) {
        return servicioUsuario.buscarUsuarios(q).stream()
                .map(mapeador::dto)
                .toList();
    }

    /**
     * Establece una relación de seguimiento entre el usuario autenticado y otro usuario.
     *
     * @param id ID del usuario al que se desea seguir.
     * @param authentication Información de sesión del seguidor.
     * @return 204 (No Content) si la operación tiene éxito.
     */
    @PostMapping("/usuarios/{id}/seguidores")
    public ResponseEntity<Void> seguir(@PathVariable Long id, Authentication authentication) {
        servicioUsuario.seguir(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina una relación de seguimiento establecida previamente.
     *
     * @param id ID del usuario al que se desea dejar de seguir.
     * @param authentication Información de sesión del seguidor.
     * @return 204 (No Content) si la operación tiene éxito.
     */
    @DeleteMapping("/usuarios/{id}/seguidores")
    public ResponseEntity<Void> dejarDeSeguir(@PathVariable Long id, Authentication authentication) {
        servicioUsuario.dejarDeSeguir(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
