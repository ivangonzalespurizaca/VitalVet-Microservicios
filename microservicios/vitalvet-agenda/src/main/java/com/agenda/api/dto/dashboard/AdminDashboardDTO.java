package com.agenda.api.dto.dashboard;

import com.agenda.api.dto.ComprobanteRecienteDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private BigDecimal ingresosTotalesHoy;
    private Long cantidadCitasHoy;
    private Long cantidadPacientesRegistrados;
    private List<ComprobanteRecienteDTO> ultimosPagos;
    private Map<String, Long> citasPorEstado;
}