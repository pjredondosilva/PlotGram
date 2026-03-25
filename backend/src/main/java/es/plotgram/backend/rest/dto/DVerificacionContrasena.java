package es.plotgram.backend.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record DVerificacionContrasena(
        @NotBlank(message = "La contraseña actual es obligatoria.")
        String contrasenaActual
) {
}