package es.plotgram.backend.rest;

import es.plotgram.backend.excepciones.UsuarioYaRegistrado;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
