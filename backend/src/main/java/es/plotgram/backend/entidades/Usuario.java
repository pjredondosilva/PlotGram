package es.plotgram.backend.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(nullable = false)
    private String contrasena;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipousuario tipo = Tipousuario.USER;

    @Column(nullable = false)
    private boolean borrado;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lista> listas = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(Long id, String nombre, String contrasena, String email, Tipousuario tipo, boolean borrado) {
        this.id = id;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.email = email;
        this.tipo = tipo;
        this.borrado = borrado;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public String getEmail() {
        return email;
    }

    public Tipousuario getTipo() {
        return tipo;
    }

    public boolean isBorrado() {
        return borrado;
    }

    public List<Lista> getListas() {
        return listas;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTipo(Tipousuario tipo) {
        this.tipo = tipo;
    }

    public void setBorrado(boolean borrado) {
        this.borrado = borrado;
    }

    public void setListas(List<Lista> listas) {
        this.listas = listas;
    }

    public void aniadirLista(Lista lista) {
        listas.add(lista);
        lista.setUsuario(this);
    }

    public void borrarLista(Lista lista) {
        listas.remove(lista);
        lista.setUsuario(null);
    }
}