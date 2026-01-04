package es.plotgram.backend.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DUsuarioRegistro(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 3, max = 30, message = "El nombre debe tener entre 3 y 30 caracteres.")
        String nombre,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[.,!@#$%^&*()_\\-+=\\[\\]{};:'\"\\\\|<>/?]).{8,72}$",
                message = "Contraseña débil: mínimo 8 caracteres, 1 mayúscula, 1 número y 1 carácter especial (por ejemplo . o ,)."
        )
        String contrasenia,

        @NotBlank(message = "El email es obligatorio.")
        @Email(message = "El email no tiene un formato válido.")
        String email
) {}

