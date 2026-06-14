package com.pacientes.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "consulta", schema = "pacientes", indexes = {
        @Index(name = "idx_consulta_cita", columnList = "id_cita")
})
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Long idConsulta;

    @Column(name = "id_cita", nullable = false, unique = true)
    private Long idCita;

    @Column(name = "id_veterinario", nullable = false)
    private Long idVeterinario;

    @Column(name = "peso_actual", precision = 5, scale = 2)
    private BigDecimal pesoActual;

    @Column(precision = 4, scale = 2)
    private BigDecimal temperatura;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String recomendaciones;

    @JsonIgnoreProperties("consulta")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "consulta")
    private List<VacunaAplicada> vacunasAplicadas;

}
