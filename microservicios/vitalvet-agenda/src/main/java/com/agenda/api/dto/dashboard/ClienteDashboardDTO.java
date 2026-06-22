package com.agenda.api.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClienteDashboardDTO {
    private String nombreCliente;
    private Long cantidadMascotas;
    private List<CitaProximaDTO> proximasCitas;
    private List<AlertaVacunaDTO> vacunasProximas;
}