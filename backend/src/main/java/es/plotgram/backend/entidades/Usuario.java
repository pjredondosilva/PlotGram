package es.plotgram.backend.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a un usuario registrado en la plataforma.
 * Contiene información de perfil, credenciales y roles.
 */
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

    @Size(max = 255)
    @Column(length = 255)
    private String fotoPerfil;

    @Size(max = 300)
    @Column(length = 300)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipousuario tipo = Tipousuario.USER;

    @Column(nullable = false)
    private boolean borrado;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lista> listas = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "seguidores",
        joinColumns = @JoinColumn(name = "seguido_id"),
        inverseJoinColumns = @JoinColumn(name = "seguidor_id")
    )
    private List<Usuario> seguidores = new ArrayList<>();

    @ManyToMany(mappedBy = "seguidores")
    private List<Usuario> seguidos = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(Long id, String nombre, String contrasena, String email,
                   String fotoPerfil, String descripcion,
                   Tipousuario tipo, boolean borrado) {
        this.id = id;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.email = email;
        this.fotoPerfil = fotoPerfil;
        this.descripcion = descripcion;
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

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public String getDescripcion() {
        return descripcion;
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

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public List<Usuario> getSeguidores() {
        return seguidores;
    }

    public List<Usuario> getSeguidos() {
        return seguidos;
    }

    /**
     * Establece una relación de seguimiento, añadiendo al usuario indicado a la
     * lista de seguidos y registrándose a sí mismo en la lista de seguidores del otro.
     *
     * @param usuario Usuario al que se desea seguir.
     */
    public void seguir(Usuario usuario) {
        if (!this.seguidos.contains(usuario)) {
            this.seguidos.add(usuario);
            usuario.getSeguidores().add(this);
        }
    }

    /**
     * Elimina una relación de seguimiento, quitando al usuario indicado de la
     * lista de seguidos y dándose de baja de la lista de seguidores del otro.
     *
     * @param usuario Usuario al que se desea dejar de seguir.
     */
    public void dejarDeSeguir(Usuario usuario) {
        this.seguidos.remove(usuario);
        usuario.getSeguidores().remove(this);
    }

}