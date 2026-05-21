package es.plotgram.backend.rest;

import es.plotgram.backend.excepciones.*;
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

/**
 * Controlador de asesoramiento global para la API REST.
 * Se encarga de interceptar y unificar el tratamiento de excepciones producidas
 * en las peticiones de los controladores, convirtiéndolas en respuestas JSON estandarizadas.
 */
@RestControllerAdvice
public class ManejadorExcepcionesApi {

    /**
     * DTO que representa un error estandarizado de la API.
     *
     * @param code Código único que identifica el tipo de error.
     * @param message Mensaje descriptivo de cara al cliente.
     * @param fieldErrors Errores específicos de campo (para validaciones de formularios).
     */
    public record ApiError(String code, String message, Map<String, String> fieldErrors) {}

    /**
     * Maneja el caso de registro duplicado de un usuario (nombre o email ya en uso).
     *
     * @param ex Excepción de usuario ya registrado.
     * @return Respuesta con estado 409 (Conflict) y detalle del error.
     */
    @ExceptionHandler(UsuarioYaRegistrado.class)
    public ResponseEntity<ApiError> manejarUsuarioYaRegistrado(UsuarioYaRegistrado ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("USUARIO_YA_EXISTE", ex.getMessage(), Map.of(ex.getCampo(), ex.getMessage())));
    }

    /**
     * Maneja el caso de nombre de lista ya registrado para un mismo usuario.
     *
     * @param ex Excepción de lista ya registrada.
     * @return Respuesta con estado 409 (Conflict) y detalle del error.
     */
    @ExceptionHandler(ListaYaRegistrada.class)
    public ResponseEntity<ApiError> manejarListaYaRegistrada(ListaYaRegistrada ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("LISTA_YA_EXISTE", ex.getMessage(), Map.of(ex.getCampo(), ex.getMessage())));
    }

    /**
     * Maneja errores de validación de argumentos en peticiones (ej. campos de formularios inválidos).
     *
     * @param ex Excepción de validación de argumentos.
     * @return Respuesta con estado 400 (Bad Request) y mapa de errores por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ApiError("ERROR_VALIDACION", "Hay campos inválidos.", errors));
    }

    /**
     * Maneja violaciones de restricciones de integridad de base de datos no capturadas a nivel lógico.
     *
     * @param ex Excepción de violación de integridad de datos.
     * @return Respuesta con estado 409 (Conflict).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarIntegridadDatos(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("INTEGRIDAD_DATOS", "Nombre o email ya registrado.", null));
    }

    /**
     * Maneja credenciales inválidas (usuario o contraseña incorrectos) en el login.
     *
     * @param ex Excepción de credenciales incorrectas.
     * @return Respuesta con estado 401 (Unauthorized).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> manejarCredencialesInvalidas(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("AUTH_INVALID", "Nombre o contraseña incorrectos.", null));
    }

    /**
     * Maneja la expiración o invalidez de tokens JWT.
     *
     * @param ex Excepción de procesamiento de JWT.
     * @return Respuesta con estado 401 (Unauthorized).
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> manejarJwtInvalido(JwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("TOKEN_INVALID", "La sesión no es válida o ha caducado.", null));
    }

    /**
     * Maneja respuestas de error HTTP devueltas por la API de TMDB.
     *
     * @param ex Excepción de WebClient para respuestas HTTP fallidas de TMDB.
     * @return Respuesta con estado 502 (Bad Gateway).
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> manejarErrorTmdb(WebClientResponseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("TMDB_ERROR", "Error al consultar el servicio externo (TMDB).", null));
    }

    /**
     * Maneja fallos de conectividad (de red o DNS) al intentar contactar con la API externa de TMDB.
     *
     * @param ex Excepción de WebClient para fallos de red.
     * @return Respuesta con estado 502 (Bad Gateway).
     */
    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ApiError> manejarConexionTmdb(WebClientRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("TMDB_UNREACHABLE", "No se ha podido contactar con TMDB.", null));
    }

    /**
     * Capturador genérico para cualquier otra excepción no controlada explícitamente.
     *
     * @param ex Excepción no manejada.
     * @return Respuesta con estado 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGenerica(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("ERROR_INTERNO", "Se ha producido un error inesperado.", null));
    }

    /**
     * Maneja el caso de búsqueda de una lista inexistente.
     *
     * @param ex Excepción de lista no encontrada.
     * @return Respuesta con estado 404 (Not Found).
     */
    @ExceptionHandler(ListaNoEncontrada.class)
    public ResponseEntity<ApiError> manejarListaNoEncontrada(ListaNoEncontrada ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("LISTA_NO_ENCONTRADA", ex.getMessage(), null));
    }

    /**
     * Maneja el intento de duplicar un contenido dentro de una misma lista.
     *
     * @param ex Excepción de contenido ya registrado en la lista.
     * @return Respuesta con estado 409 (Conflict).
     */
    @ExceptionHandler(ContenidoYaEnLista.class)
    public ResponseEntity<ApiError> manejarContenidoYaEnLista(ContenidoYaEnLista ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("CONTENIDO_YA_EN_LISTA", ex.getMessage(), null));
    }

    /**
     * Maneja el intento de interactuar con un elemento que no existe en una lista.
     *
     * @param ex Excepción de elemento no encontrado en la lista.
     * @return Respuesta con estado 404 (Not Found).
     */
    @ExceptionHandler(ElementoNoEncontradoEnLista.class)
    public ResponseEntity<ApiError> manejarElementoNoEncontradoEnLista(ElementoNoEncontradoEnLista ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("ELEMENTO_NO_ENCONTRADO_EN_LISTA", ex.getMessage(), null));
    }

    /**
     * Maneja búsquedas de usuarios que no existen en la base de datos.
     *
     * @param ex Excepción de usuario no encontrado.
     * @return Respuesta con estado 404 (Not Found).
     */
    @ExceptionHandler(UsuarioNoEncontrado.class)
    public ResponseEntity<ApiError> manejarUsuarioNoEncontrado(UsuarioNoEncontrado ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("USUARIO_NO_ENCONTRADO", ex.getMessage(), null));
    }

    /**
     * Maneja errores cuando la contraseña actual no coincide con la almacenada al intentar actualizar perfil.
     *
     * @param ex Excepción de contraseña actual incorrecta.
     * @return Respuesta con estado 401 (Unauthorized).
     */
    @ExceptionHandler(ContrasenaActualIncorrecta.class)
    public ResponseEntity<ApiError> manejarContrasenaActualIncorrecta(ContrasenaActualIncorrecta ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("CONTRASENA_ACTUAL_INCORRECTA", ex.getMessage(), null));
    }

}
