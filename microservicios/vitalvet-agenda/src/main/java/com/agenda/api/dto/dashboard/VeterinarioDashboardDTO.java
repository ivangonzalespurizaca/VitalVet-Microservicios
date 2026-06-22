package com.agenda.api.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VeterinarioDashboardDTO {
    private Long pacientesEnEspera;
    private Long pacientesAtendidosHoy;
    private String siguientePaciente;
    private List<CitaProximaDTO> atencionesRecientes;

    private List<CitaProximaDTO> proximasCitas;
}