package es.plotgram.backend.rest;

import es.plotgram.backend.excepciones.UsuarioYaRegistrado;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorExcepcionesApi {

    public record ApiError(String code, String message, Map<String, String> fieldErrors) {}

    @ExceptionHandler(UsuarioYaRegistrado.class)
    public ResponseEntity<ApiError> manejarUsuarioYaRegistrado(UsuarioYaRegistrado ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("USUARIO_YA_EXISTE", ex.getMessage(), Map.of(ex.getCampo(), ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ApiError("ERROR_VALIDACION", "Hay campos inválidos.", errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarIntegridadDatos(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("INTEGRIDAD_DATOS", "Nombre o email ya registrado.", null));
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> manejarCredencialesInvalidas(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("AUTH_INVALID", "Nombre o contraseña incorrectos.", null));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> manejarJwtInvalido(JwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("TOKEN_INVALID", "La sesión no es válida o ha caducado.", null));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> manejarErrorTmdb(WebClientResponseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("TMDB_ERROR", "Error al consultar el servicio externo (TMDB).", null));
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ApiError> manejarConexionTmdb(WebClientRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("TMDB_UNREACHABLE", "No se ha podido contactar con TMDB.", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGenerica(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("ERROR_INTERNO", "Se ha producido un error inesperado.", null));
    }
}
