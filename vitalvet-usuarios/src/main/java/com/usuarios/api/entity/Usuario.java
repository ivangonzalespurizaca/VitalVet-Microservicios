package com.usuarios.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario", indexes = {
        @Index(name = "idx_usuario_fecha", columnList = "fecha_creacion")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String contrasenia;

    @Column(name = "token_recuperacion", length = 255)
    private String tokenRecuperacion;

    @Column(name = "fecha_expiracion_token")
    private LocalDateTime fechaExpiracionToken;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "id_persona", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_usuario_persona"))
    private Persona persona;

}
