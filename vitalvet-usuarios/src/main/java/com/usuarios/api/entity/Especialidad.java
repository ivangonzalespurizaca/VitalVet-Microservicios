package com.usuarios.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "especialidad")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especialidad")
    private Long idEspecialidad;

    @Column(name = "nombre_especialidad", nullable = false, length = 100, unique = true)
    private String nombreEspecialidad;

    @Column(nullable = false)
    private Boolean activo = true;

    @JsonIgnoreProperties("especialidad")
    @ToString.Exclude
    @OneToMany(mappedBy = "especialidad", cascade = CascadeType.ALL)
    private List<Veterinario> veterinarios;
}
