package es.plotgram.backend.rest.dto;
import jakarta.validation.constraints.NotBlank;

public record DAutenticacionUsuario(@NotBlank String nombre, @NotBlank String contrasenia) {
}
