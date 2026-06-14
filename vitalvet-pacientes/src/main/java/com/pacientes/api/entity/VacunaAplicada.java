package com.pacientes.api.entity;


import com.pacientes.api.entity.enums.EstadoVacuna;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vacuna_aplicada", schema = "pacientes", indexes = {
        @Index(name = "idx_vacuna_aplicada_mascota", columnList = "id_mascota")
})
public class VacunaAplicada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aplicacion")
    private Long idAplicacion;

    @Column(name = "fecha_aplicacion")
    private LocalDate fechaAplicacion;

    @Column(name = "proxima_dosis")
    private LocalDate proximaDosis;

    @Column(name = "nro_dosis", length = 50)
    private String nroDosis = "1ra Dosis";

    @Enumerated(EnumType.STRING)
    private EstadoVacuna estado = EstadoVacuna.APLICADA;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mascota", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vacuna_mascota"))
    private Mascota mascota;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vacuna", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vacuna_catalogo"))
    private Vacuna vacuna;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consulta",
            foreignKey = @ForeignKey(name = "fk_vacuna_consulta"))
    private Consulta consulta;

}
