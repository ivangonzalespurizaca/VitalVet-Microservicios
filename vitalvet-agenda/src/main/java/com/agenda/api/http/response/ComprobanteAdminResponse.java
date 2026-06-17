package com.agenda.api.http.response;

import com.agenda.api.dto.ComprobanteDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComprobanteAdminResponse {
    private List<ComprobanteDTO> contenido;
    private Long totalComprobantesEmitidos;
    private BigDecimal montoTotalRecaudado;
}
