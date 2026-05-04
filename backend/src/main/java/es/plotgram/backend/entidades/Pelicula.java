package es.plotgram.backend.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Representa una película en el sistema.
 */
@Entity
@DiscriminatorValue("PELICULA")
public class Pelicula extends Contenido {

    public Pelicula() {
    }

    @Override
    public TipoContenido getTipo() {
        return TipoContenido.PELICULA;
    }
}