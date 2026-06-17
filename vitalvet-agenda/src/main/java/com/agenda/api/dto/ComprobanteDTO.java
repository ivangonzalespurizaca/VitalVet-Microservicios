package com.agenda.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComprobanteDTO {
    private String tipoComprobante;
    private String codigoComprobante;
    private LocalDateTime fechaPago;
    private String metodoPago;
    private BigDecimal montoTotal;
    private String nombreCliente;
    private BigDecimal montoSubtotal;
    private BigDecimal montoImpuesto;
    private String dniCliente;
}