package es.plotgram.backend.rest.dto;

import es.plotgram.backend.entidades.TipoContenido;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DContenidoListaNuevo(

        @NotNull(message = "El tmdbId es obligatorio.")
        @Min(value = 1, message = "El tmdbId debe ser mayor que 0.")
        Long tmdbId,

        @NotNull(message = "El tipo de contenido es obligatorio.")
        TipoContenido tipo,

        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres.")
        String titulo,

        @Size(max = 255, message = "La imagen no puede superar los 255 caracteres.")
        String imagen,

        LocalDate fechaPublicacion,

        @Size(max = 2000, message = "La sinopsis no puede superar los 2000 caracteres.")
        String sinopsis,

        @NotBlank(message = "El enlace es obligatorio.")
        @Size(max = 255, message = "El enlace no puede superar los 255 caracteres.")
        String enlace,

        @Min(value = 1, message = "El identificador de serie debe ser mayor que 0.")
        Long serieTmdbId,

        @Min(value = 0, message = "El número de temporada no puede ser negativo.")
        Integer numeroTemporada,

        @Min(value = 1, message = "El número de episodio debe ser mayor que 0.")
        Integer numeroEpisodio
) {
}