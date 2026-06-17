package com.agenda.api.entity;

import com.agenda.api.entity.enums.DiaSemana;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "horario_atencion", schema = "agenda",
        uniqueConstraints = {@UniqueConstraint(name = "uq_veterinario_dia", columnNames = {"id_veterinario", "dia_semana"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Long idHorario;

    @Column(name = "id_veterinario", nullable = false)
    private Long idVeterinario;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DiaSemana diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    private Boolean activo = true;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos = 60;
}