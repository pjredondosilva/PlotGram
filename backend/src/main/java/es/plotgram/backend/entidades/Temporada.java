package es.plotgram.backend.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Representa una temporada específica de una serie.
 */
@Entity
@DiscriminatorValue("TEMPORADA")
public class Temporada extends ContenidoDeSerie {

    public Temporada() {
    }

    @Override
    public TipoContenido getTipo() {
        return TipoContenido.TEMPORADA;
    }
}