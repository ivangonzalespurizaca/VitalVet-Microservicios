package com.pacientes.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "raza", schema = "pacientes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_especie_raza", columnNames = {"id_especie", "nombre_raza"})
})
public class Raza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_raza")
    private Long idRaza;

    @Column(name = "nombre_raza", nullable = false, length = 100)
    private String nombreRaza;

    @Column(nullable = false)
    private Boolean activo = true;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_especie", nullable = false,
            foreignKey = @ForeignKey(name = "fk_raza_especie"))
    private Especie especie;

    @JsonIgnoreProperties("raza")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "raza")
    private List<Mascota> mascotas;

}
