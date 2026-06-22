package com.agenda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CasoObservacionDTO {
    private Long idCita;         // El puente para buscar en Agenda
    private String nombreMascota;
    private String raza;
    private String diagnostico;
    private String estado;
}