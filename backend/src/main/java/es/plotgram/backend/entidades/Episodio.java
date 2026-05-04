package es.plotgram.backend.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Representa un episodio individual dentro de una temporada de una serie.
 */
@Entity
@DiscriminatorValue("EPISODIO")
public class Episodio extends ContenidoDeSerie {

    @Min(1)
    @Column
    private Integer numeroEpisodio;

    public Episodio() {
    }

    @Override
    public TipoContenido getTipo() {
        return TipoContenido.EPISODIO;
    }

    @Override
    public Integer getNumeroEpisodio() {
        return numeroEpisodio;
    }

    public void setNumeroEpisodio(Integer numeroEpisodio) {
        this.numeroEpisodio = numeroEpisodio;
    }
}