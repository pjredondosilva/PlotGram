package es.plotgram.backend.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public abstract class contenidoDeSerie extends Contenido {

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Long serieTmdbId;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer numeroTemporada;

    public contenidoDeSerie() {
    }

    @Override
    public Long getSerieTmdbId() {
        return serieTmdbId;
    }

    @Override
    public Integer getNumeroTemporada() {
        return numeroTemporada;
    }

    public void setSerieTmdbId(Long serieTmdbId) {
        this.serieTmdbId = serieTmdbId;
    }

    public void setNumeroTemporada(Integer numeroTemporada) {
        this.numeroTemporada = numeroTemporada;
    }
}