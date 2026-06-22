package com.agenda.api.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CitaProximaDTO {
    private String nombreMascota;
    private String fecha;
    private String hora;
    private String motivo;
    private String estado;
}