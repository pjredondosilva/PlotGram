package es.plotgram.backend.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Clase abstracta para contenidos que pertenecen a una serie (Temporadas y Episodios).
 * Mantiene la vinculación con la serie madre mediante su ID de TMDB.
 */
@Entity
public abstract class ContenidoDeSerie extends Contenido {

    @Min(1)
    @Column
    private Long serieTmdbId;

    @Min(0)
    @Column
    private Integer numeroTemporada;

    public ContenidoDeSerie() {
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