package com.usuarios.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.usuarios.api.entity.enums.Genero;
import com.usuarios.api.entity.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "persona", indexes = {
        @Index(name = "idx_persona_rol", columnList = "rol"),
        @Index(name = "idx_persona_codigo", columnList = "codigo_persona")
})
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long idPersona;

    @Column(name = "codigo_persona", nullable = false, length = 20, unique = true)
    private String codigoPersona;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, length = 20, unique = true)
    private String dni;

    @Column(length = 15)
    private String celular;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Genero genero = Genero.NO_ESPECIFICADO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private Boolean activo = true;

    @ToString.Exclude
    @JsonIgnoreProperties("persona")
    @OneToOne(mappedBy = "persona")
    private Veterinario veterinario;

    @ToString.Exclude
    @JsonIgnoreProperties("persona")
    @OneToOne(mappedBy = "persona")
    private Usuario usuario;
}
