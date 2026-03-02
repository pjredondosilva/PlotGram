package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.DAutenticacionUsuario;
import es.plotgram.backend.seguridad.AutenticacionPorTokens.UtilJwt;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ControladorToken {

    @Autowired
    AuthenticationManager authenticationManager;

    @Value("${tiempoExpiracionTokenJwtMin}")
    int tiempoExpiracionToken;

    @Value("${app.auth.jwt.refreshWindowMin:5}")
    int refreshWindowMin;

    @Value("${app.auth.jwt.maxSessionHours:8}")
    int maxSessionHours;

    //poner a true en producción
    @Value("${app.auth.cookie.secure:false}")
    boolean cookieSecure;

    @Value("${app.auth.cookie.samesite:Lax}")
    String cookieSameSite;

    @PostMapping("/sesiones")
    public ResponseEntity<?> obtenerToken(@Valid @RequestBody DAutenticacionUsuario datosLogin) {
        Authentication authentication;

            authentication =  authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(datosLogin.nombre(), datosLogin.contrasenia())
            );

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long sessionStart = Instant.now().toEpochMilli();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("session_start", sessionStart);

        String token = UtilJwt.crearToken(
                String.valueOf(datosLogin.nombre()),
                claims,
                tiempoExpiracionToken
        );

        ResponseCookie cookie = ResponseCookie.from("pg_token", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(tiempoExpiracionToken * 60L)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(Map.of("ok", true));
    }

    @DeleteMapping("/sesiones/actual")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("pg_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(Map.of("ok", true));
    }

    @PostMapping("/sesiones/refrescar")
    public ResponseEntity<?> Refrescar(@CookieValue(name = "pg_token", required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ManejadorExcepcionesApi.ApiError("AUTH_REQUIRED", "No hay sesión activa.", null));
        }

        Claims claims = UtilJwt.extraerContenido(token);

        Instant now = Instant.now();
        Instant exp = claims.getExpiration().toInstant();

        long remainingSeconds = Duration.between(now, exp).getSeconds();
        if (remainingSeconds > refreshWindowMin * 60L) {
            return ResponseEntity.noContent().build(); // todavía no toca
        }

        Long sessionStartMs = claims.get("session_start", Long.class);
        if (sessionStartMs == null) {
            sessionStartMs = now.toEpochMilli();
        }

        Instant sessionStart = Instant.ofEpochMilli(sessionStartMs);
        Instant hardEnd = sessionStart.plus(Duration.ofHours(maxSessionHours));

        if (!now.isBefore(hardEnd)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ManejadorExcepcionesApi.ApiError("SESSION_EXPIRED", "La sesión ha caducado. Inicia sesión de nuevo.", null));
        }

        Instant desiredExp = now.plus(Duration.ofMinutes(tiempoExpiracionToken));
        Instant newExp = desiredExp.isBefore(hardEnd) ? desiredExp : hardEnd;

        Map<String, Object> newClaims = new HashMap<>(claims);
        newClaims.remove("exp");
        newClaims.remove("iat");

        String newToken = UtilJwt.crearTokenConExp(claims.getSubject(), newClaims, java.util.Date.from(newExp));

        long maxAgeSeconds = Duration.between(now, newExp).getSeconds();

        ResponseCookie cookie = ResponseCookie.from("pg_token", newToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(Map.of("refreshed", true));
    }
}
