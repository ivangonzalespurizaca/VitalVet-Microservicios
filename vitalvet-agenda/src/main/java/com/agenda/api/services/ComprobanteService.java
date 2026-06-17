package com.agenda.api.services;

import com.agenda.api.http.response.ComprobanteAdminResponse;
import com.agenda.api.http.response.ComprobanteClienteResponse;

import java.time.LocalDate;

public interface ComprobanteService {
    ComprobanteAdminResponse obtenerHistorialComprobantes(String criterio, String tipo, LocalDate inicio, LocalDate fin);
    ComprobanteClienteResponse obtenerComprobantesPorCliente(Long idCliente, String tipo, LocalDate inicio, LocalDate fin);
}
