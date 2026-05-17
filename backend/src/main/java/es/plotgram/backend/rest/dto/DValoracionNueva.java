package es.plotgram.backend.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DValoracionNueva(
        @NotNull
        DContenidoListaNuevo contenido,

        @Min(1)
        @Max(5)
        int puntuacion,

        @Size(max = 2000)
        String comentario
) {}
