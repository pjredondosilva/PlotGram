package es.plotgram.backend.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Representa una serie de televisión en el sistema.
 */
@Entity
@DiscriminatorValue("SERIE")
public class Serie extends Contenido {

    public Serie() {
    }

    @Override
    public TipoContenido getTipo() {
        return TipoContenido.SERIE;
    }
}