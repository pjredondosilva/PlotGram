package es.plotgram.backend.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Entidad base para cualquier tipo de contenido (Película, Serie, Temporada,
 * Episodio).
 * Utiliza una estrategia de tabla única (SINGLE_TABLE) para la herencia y
 * expone el
 * tipo de contenido mediante un método abstracto.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "tmdbId", "tipo" })
})
public abstract class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Long tmdbId;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String titulo;

    @Size(max = 255)
    @Column(length = 255)
    private String imagen;

    private LocalDate fechaPublicacion;

    @Size(max = 2000)
    @Column(length = 2000)
    private String sinopsis;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String enlace;

    public Contenido() {
    }

    public Long getId() {
        return id;
    }

    public Long getTmdbId() {
        return tmdbId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getImagen() {
        return imagen;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public String getEnlace() {
        return enlace;
    }

    public void setTmdbId(Long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public abstract TipoContenido getTipo();

    public Long getSerieTmdbId() {
        return null;
    }

    public Integer getNumeroTemporada() {
        return null;
    }

    public Integer getNumeroEpisodio() {
        return null;
    }
}