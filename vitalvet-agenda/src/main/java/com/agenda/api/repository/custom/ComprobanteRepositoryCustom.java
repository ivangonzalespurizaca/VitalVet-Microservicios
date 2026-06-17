package com.agenda.api.repository.custom;

import com.agenda.api.entity.ComprobantePago;
import com.agenda.api.entity.enums.TipoComprobante;

import java.time.LocalDate;
import java.util.List;

public interface ComprobanteRepositoryCustom {
    List<ComprobantePago> listarConFiltrosAvanzados(
            String criterio,
            TipoComprobante tipo,
            LocalDate fechaInicio,
            LocalDate fechaFin);

    List<ComprobantePago> listarParaClienteConFiltros(
            Long idCliente, TipoComprobante tipo, LocalDate fechaInicio, LocalDate fechaFin);
}