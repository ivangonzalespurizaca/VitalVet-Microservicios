package com.vitalvet.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsultaResumenDTO {
    private String nombreMascota;
    private String fechaConsulta;
    private String diagnostico;
    private String veterinarioNombre;
}