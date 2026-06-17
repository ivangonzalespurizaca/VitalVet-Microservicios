package com.agenda.api.dto;

import com.agenda.api.entity.enums.DiaSemana;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class HorarioRequestDTO {
    @NotNull(message = "El ID del veterinario es obligatorio.")
    private Long idVeterinario;

    @NotNull(message = "El día de la semana es obligatorio.")
    private DiaSemana diaSemana; // Mapea directo con tu Enum

    @NotNull(message = "La hora de inicio es obligatoria.")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria.")
    private LocalTime horaFin;
}
