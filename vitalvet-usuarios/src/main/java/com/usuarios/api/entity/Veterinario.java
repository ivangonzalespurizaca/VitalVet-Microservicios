package com.usuarios.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "veterinario")
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_veterinario")
    private Long idVeterinario;

    @Column(name = "num_colegiatura", nullable = false, length = 20, unique = true)
    private String numColegiatura;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "id_persona", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_vet_persona"))
    private Persona persona;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "id_especialidad", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vet_especialidad"))
    private Especialidad especialidad;

}
