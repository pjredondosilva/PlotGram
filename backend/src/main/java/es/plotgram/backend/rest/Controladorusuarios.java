package es.plotgram.backend.rest;

import es.plotgram.backend.entidades.Usuario;
import es.plotgram.backend.excepciones.UsuarioYaRegistrado;
import es.plotgram.backend.rest.dto.Dusuario;
import es.plotgram.backend.rest.dto.Mapeador;
import es.plotgram.backend.servicios.ServicioUsuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class Controladorusuarios {
    @Autowired
    Mapeador mapeador;
    @Autowired
    ServicioUsuario serviciousuario;

    /**
     * Registro de usuario
     * POST /api/usuarios
     * 201 si ok, 409 si email ya existe
     */
    @PostMapping("/usuarios")
    public ResponseEntity<Void> nuevoUsuario(@Valid @RequestBody Dusuario dto) {
        try {
            serviciousuario.nuevoUsuario(mapeador.entidadNueva(dto));
        }
        catch(UsuarioYaRegistrado e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * Obtener usuario por id
     * GET /api/usuarios/{id}
     * 200 si existe, 404 si no
     */
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Dusuario> obtenerUsuario(@PathVariable long id) {
        return serviciousuario.buscarUsuario(id)
                .map(u -> ResponseEntity.ok(mapeador.dto(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
