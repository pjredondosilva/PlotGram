package es.plotgram.backend.entidades;

import jakarta.persistence.*;
import lombok.*;

import jakarta.validation.constraints.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    @Column(nullable=false, unique=true, length=30)
    private String nombre;
    @Column(nullable = false)
    private String contrasena;
    @Email
    @Column(nullable = false, unique = true)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipousuario tipo= Tipousuario.USER;
    @Column(nullable = false)
    private boolean borrado;
}
