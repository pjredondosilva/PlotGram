package es.plotgram.backend.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Representa una valoración y reseña realizada por un usuario sobre un contenido específico.
 * Incluye una puntuación numérica (1-5) y un comentario opcional.
 */
@Entity
@Table(
        name = "valoraciones",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"usuario_id", "contenido_id"})
        }
)
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contenido_id", nullable = false)
    private Contenido contenido;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int puntuacion;

    @Size(max = 2000)
    @Column(length = 2000)
    private String comentario;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fecha;

    public Valoracion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Contenido getContenido() {
        return contenido;
    }

    public void setContenido(Contenido contenido) {
        this.contenido = contenido;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
