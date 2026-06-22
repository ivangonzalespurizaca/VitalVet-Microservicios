package com.agenda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteRecienteDTO {
    private String nombreCliente;
    private String codigoComprobante;
    private BigDecimal montoTotal;
    private String metodoPago;
}