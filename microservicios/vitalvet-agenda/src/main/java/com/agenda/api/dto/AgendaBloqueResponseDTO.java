package com.agenda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendaBloqueResponseDTO {
    private String hora;
    private boolean disponible;
    private Long idCita;
    private String nombreMascota;
    private String razaMascota;
    private String nombrePropietario;
    private String dniPropietario;
    private String motivoConsulta;
}