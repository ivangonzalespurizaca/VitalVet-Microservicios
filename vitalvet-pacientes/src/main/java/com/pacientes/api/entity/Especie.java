package com.pacientes.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "especie", schema = "pacientes")
public class Especie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especie")
    private Long idEspecie;

    @Column(name = "nombre_especie", nullable = false, length = 50, unique = true)
    private String nombreEspecie;

    @Column(nullable = false)
    private Boolean activo = true;

    @JsonIgnoreProperties("especie")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "especie", cascade = CascadeType.ALL)
    private List<Raza> razas;

}
