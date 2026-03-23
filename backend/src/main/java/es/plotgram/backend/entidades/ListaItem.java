package es.plotgram.backend.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"lista_id", "contenido_id"})
        }
)
public class ListaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lista_id", nullable = false)
    private Lista lista;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contenido_id", nullable = false)
    private Contenido contenido;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer orden;

    public ListaItem() {
    }

    public ListaItem(Long id, Lista lista, Contenido contenido, Integer orden) {
        this.id = id;
        this.lista = lista;
        this.contenido = contenido;
        this.orden = orden;
    }

    public Long getId() {
        return id;
    }

    public Lista getLista() {
        return lista;
    }

    public Contenido getContenido() {
        return contenido;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setLista(Lista lista) {
        this.lista = lista;
    }

    public void setContenido(Contenido contenido) {
        this.contenido = contenido;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }
}