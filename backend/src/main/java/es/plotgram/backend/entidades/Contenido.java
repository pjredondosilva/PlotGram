package es.plotgram.backend.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tmdbId", "tipo"})
        }
)
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Long tmdbId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoContenido tipo;

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

    @Min(1)
    @Column
    private Long serieTmdbId;

    @Min(0)
    @Column
    private Integer numeroTemporada;

    @Min(1)
    @Column
    private Integer numeroEpisodio;


    public Contenido() {
    }

    public Contenido(Long id, Long tmdbId, TipoContenido tipo, String titulo, String imagen,
                            LocalDate fechaPublicacion, String sinopsis, String enlace,
                            Long serieTmdbId, Integer numeroTemporada, Integer numeroEpisodio) {
        this.id = id;
        this.tmdbId = tmdbId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.imagen = imagen;
        this.fechaPublicacion = fechaPublicacion;
        this.sinopsis = sinopsis;
        this.enlace = enlace;
        this.serieTmdbId = serieTmdbId;
        this.numeroTemporada = numeroTemporada;
        this.numeroEpisodio = numeroEpisodio;
    }

    public Long getId() {
        return id;
    }

    public Long getTmdbId() {
        return tmdbId;
    }

    public TipoContenido getTipo() {
        return tipo;
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

    public Long getSerieTmdbId() {
        return serieTmdbId;
    }

    public Integer getNumeroTemporada() {
        return numeroTemporada;
    }

    public Integer getNumeroEpisodio() {
        return numeroEpisodio;
    }

    public void setTmdbId(Long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public void setTipo(TipoContenido tipo) {
        this.tipo = tipo;
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

    public void setSerieTmdbId(Long serieTmdbId) {
        this.serieTmdbId = serieTmdbId;
    }

    public void setNumeroTemporada(Integer numeroTemporada) {
        this.numeroTemporada = numeroTemporada;
    }

    public void setNumeroEpisodio(Integer numeroEpisodio) {
        this.numeroEpisodio = numeroEpisodio;
    }

}