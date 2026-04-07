package es.plotgram.backend.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@DiscriminatorValue("EPISODIO")
public class Episodio extends ContenidoDeSerie {

    @NotNull
    @Min(1)
    @Column(nullable = false)
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