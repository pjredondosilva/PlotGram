package es.plotgram.backend.rest;

import es.plotgram.backend.rest.dto.DAutenticacionUsuario;
import es.plotgram.backend.seguridad.AutenticacionPorTokens.UtilJwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
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

/**
 * Controlador REST para la gestión de sesiones y tokens JWT.
 * Maneja el inicio de sesión, cierre de sesión y la renovación de tokens.
 */
@RestController
@RequestMapping("/api")
public class ControladorToken {

    private final AuthenticationManager authenticationManager;
    private final UtilJwt utilJwt;

    @Value("${tiempoExpiracionTokenJwtMin}")
    int tiempoExpiracionToken;

    @Value("${app.auth.jwt.refrescarventana}")
    int tiempoRefrescarSesion;

    @Value("${app.auth.jwt.maxhorassesion}")
    int TiempoMaximoSesion;

    //poner a true en producción
    @Value("${app.auth.cookie.secure:false}")
    boolean cookieSecure;

    @Value("${app.auth.cookie.samesite:Lax}")
    String cookieSameSite;


    public ControladorToken(AuthenticationManager authenticationManager, UtilJwt utilJwt) {
        this.authenticationManager = authenticationManager;
        this.utilJwt = utilJwt;
    }

    /**
     * Autentica al usuario y genera un token JWT que se envía en una cookie HTTP-only.
     *
     * @param datosLogin credenciales de acceso del usuario
     * @return respuesta con la cookie de sesión creada
     */
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

        String token = utilJwt.crearToken(
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
                .header("Cache-Control", "no-store")
                .body(Map.of("ok", true));
    }

    /**
     * Cierra la sesión actual invalidando la cookie que contiene el token.
     *
     * @return respuesta de confirmación de cierre de sesión
     */
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
                .header("Cache-Control", "no-store")
                .body(Map.of("ok", true));
    }

    /**
     * Renueva el token de la sesión actual si se encuentra dentro de la
     * ventana de renovación permitida y no se ha alcanzado la duración
     * máxima de la sesión.
     *
     * @param token token JWT almacenado en la cookie de sesión
     * @return respuesta con el nuevo token o el estado correspondiente
     */
    @PostMapping("/sesiones/renovacion")
    public ResponseEntity<?> refrescar(@CookieValue(name = "pg_token", required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ManejadorExcepcionesApi.ApiError("AUTH_REQUIRED", "No hay sesión activa.", null));
        }

        final Claims claims;
        try {
            claims = utilJwt.extraerContenido(token);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ManejadorExcepcionesApi.ApiError("TOKEN_INVALID", "La sesión no es válida o ha caducado.", null));
        }

        Instant now = Instant.now();
        Instant exp = claims.getExpiration().toInstant();

        long remainingSeconds = Duration.between(now, exp).getSeconds();
        if (remainingSeconds <= 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ManejadorExcepcionesApi.ApiError("TOKEN_EXPIRED", "La sesión ha caducado. Inicia sesión de nuevo.", null));
        }
        if (remainingSeconds > tiempoRefrescarSesion * 60L) {
            return ResponseEntity.noContent().build();
        }

        Long sessionStartMs = claims.get("session_start", Long.class);
        if (sessionStartMs == null) {
            sessionStartMs = now.toEpochMilli();
        }

        Instant sessionStart = Instant.ofEpochMilli(sessionStartMs);
        Instant hardEnd = sessionStart.plus(Duration.ofHours(TiempoMaximoSesion));

        if (!now.isBefore(hardEnd)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ManejadorExcepcionesApi.ApiError("SESSION_EXPIRED", "La sesión ha caducado. Inicia sesión de nuevo.", null));
        }

        Instant desiredExp = now.plus(Duration.ofMinutes(tiempoExpiracionToken));
        Instant newExp = desiredExp.isBefore(hardEnd) ? desiredExp : hardEnd;

        Map<String, Object> newClaims = new HashMap<>(claims);
        newClaims.remove("exp");
        newClaims.remove("iat");

        String newToken = utilJwt.crearTokenConExp(claims.getSubject(), newClaims, java.util.Date.from(newExp));

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
                .header("Cache-Control", "no-store")
                .body(Map.of("refreshed", true));
    }

    /**
     * Devuelve información sobre el estado de la sesión actual.
     *
     * @param token token JWT almacenado en la cookie de sesión
     * @return datos de expiración y vigencia de la sesión
     */
    @GetMapping("/sesiones/estado")
    public ResponseEntity<?> estado(@CookieValue(name = "pg_token", required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ManejadorExcepcionesApi.ApiError("AUTH_REQUIRED", "No hay sesión activa.", null));
        }

        final Claims claims;
        try {
            claims = utilJwt.extraerContenido(token);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ManejadorExcepcionesApi.ApiError("TOKEN_INVALID", "La sesión no es válida o ha caducado.", null));
        }

        Instant now = Instant.now();
        Instant exp = claims.getExpiration().toInstant();
        long remainingSeconds = Math.max(0, Duration.between(now, exp).getSeconds());

        Long sessionStartMs = claims.get("session_start", Long.class);
        if (sessionStartMs == null) sessionStartMs = now.toEpochMilli();
        Instant sessionStart = Instant.ofEpochMilli(sessionStartMs);
        Instant hardEnd = sessionStart.plus(Duration.ofHours(TiempoMaximoSesion));

        long hardRemainingSeconds = Math.max(0, Duration.between(now, hardEnd).getSeconds());

        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(Map.of(
                        "usuario", claims.getSubject(),
                        "expiresAt", exp.toEpochMilli(),
                        "remainingSeconds", remainingSeconds,
                        "refreshWindowSeconds", tiempoRefrescarSesion * 60L,
                        "hardEndAt", hardEnd.toEpochMilli(),
                        "hardRemainingSeconds", hardRemainingSeconds
                ));
    }
}
