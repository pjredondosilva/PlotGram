package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.DAutenticacionUsuario;
import es.plotgram.backend.seguridad.AutenticacionPorTokens.UtilJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ControladorToken {

    @Autowired
    AuthenticationManager authenticationManager;

    @Value("${tiempoExpiracionTokenJwtMin}")
    int tiempoExpiracionToken;

    @PostMapping("/autenticacion")
    public ResponseEntity<?> obtenerToken(@RequestBody DAutenticacionUsuario datosLogin) {
        Authentication authentication;
        try {
            authentication =  authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(datosLogin.nombre(), datosLogin.contrasenia())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "code", "AUTH_INVALID",
                    "message", "Nombre o contraseña incorrectos."
            ));
        }

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String token = UtilJwt.crearToken(
                String.valueOf(datosLogin.nombre()),
                Collections.singletonMap("roles", roles),
                tiempoExpiracionToken
        );

        return ResponseEntity.ok(token);
    }
}