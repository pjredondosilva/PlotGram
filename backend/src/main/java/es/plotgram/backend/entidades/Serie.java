package es.plotgram.backend.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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