package com.agenda.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConsultaDetalleDTO {
    private Long idConsulta;
    private BigDecimal pesoActual;
    private BigDecimal temperatura;
    private String diagnostico;
    private String recomendaciones;
}