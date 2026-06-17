package com.agenda.api.services.impl;

import com.agenda.api.client.UsuarioClient;
import com.agenda.api.dto.ClienteResponseDTO;
import com.agenda.api.dto.ComprobanteDTO;
import com.agenda.api.entity.ComprobantePago;
import com.agenda.api.entity.enums.TipoComprobante;
import com.agenda.api.http.response.ComprobanteAdminResponse;
import com.agenda.api.http.response.ComprobanteClienteResponse;
import com.agenda.api.repository.ComprobanteRepository;
import com.agenda.api.services.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ComprobanteServiceImpl implements ComprobanteService {
    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Autowired
    private UsuarioClient usuarioClient;

    @Override
    public ComprobanteAdminResponse obtenerHistorialComprobantes(String criterio, String tipo, LocalDate inicio, LocalDate fin) {
        TipoComprobante tipoEnum = (tipo != null && !tipo.isEmpty()) ? TipoComprobante.valueOf(tipo) : null;

        List<ComprobantePago> resultados = comprobanteRepository.listarConFiltrosAvanzados(criterio, tipoEnum, inicio, fin);

        long totalEmitidos = resultados.size();
        BigDecimal totalRecaudado = resultados.stream()
                .map(ComprobantePago::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ComprobanteDTO> contenidoDto = resultados.stream().map(c -> {
            String nombreCompleto = "Cliente ID: " + c.getIdCliente();
            String dni = "";

            try {
                ClienteResponseDTO cliente = usuarioClient.obtenerDetalleCliente(c.getIdCliente());
                if (cliente != null) {
                    nombreCompleto = cliente.getNombres() + " " + cliente.getApellidos();
                    dni = cliente.getDni();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ComprobanteDTO.builder()
                    .tipoComprobante(c.getTipoDocumento().name())
                    .codigoComprobante(c.getCodigoComprobante())
                    .fechaPago(c.getFechaPago())
                    .metodoPago(c.getMetodoPago().name())
                    .montoSubtotal(c.getMontoSubtotal())
                    .montoImpuesto(c.getMontoImpuesto())
                    .montoTotal(c.getMontoTotal())
                    .nombreCliente(nombreCompleto)
                    .dniCliente(dni)
                    .build();
        }).toList();

        ComprobanteAdminResponse response = new ComprobanteAdminResponse();
        response.setContenido(contenidoDto);
        response.setTotalComprobantesEmitidos(totalEmitidos);
        response.setMontoTotalRecaudado(totalRecaudado);

        return response;
    }

    @Override
    public ComprobanteClienteResponse obtenerComprobantesPorCliente(Long idCliente, String tipo, LocalDate inicio, LocalDate fin) {
        TipoComprobante tipoEnum = (tipo != null && !tipo.isEmpty()) ? TipoComprobante.valueOf(tipo) : null;
        List<ComprobantePago> resultados = comprobanteRepository.listarParaClienteConFiltros(idCliente, tipoEnum, inicio, fin);
        String nombreCompleto = "Cliente ID: " + idCliente;
        String dni = "";
        try {
            ClienteResponseDTO cliente = usuarioClient.obtenerDetalleCliente(idCliente);
            if (cliente != null) {
                nombreCompleto = cliente.getNombres() + " " + cliente.getApellidos();
                dni = cliente.getDni();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String nombreFinal = nombreCompleto;
        final String dniFinal = dni;

        List<ComprobanteDTO> contenidoDto = resultados.stream().map(c ->
                ComprobanteDTO.builder()
                        .tipoComprobante(c.getTipoDocumento().name())
                        .codigoComprobante(c.getCodigoComprobante())
                        .fechaPago(c.getFechaPago())
                        .metodoPago(c.getMetodoPago().name())
                        .montoSubtotal(c.getMontoSubtotal())
                        .montoImpuesto(c.getMontoImpuesto())
                        .montoTotal(c.getMontoTotal())
                        .nombreCliente(nombreFinal)
                        .dniCliente(dniFinal)
                        .build()
        ).toList();

        ComprobanteClienteResponse response = new ComprobanteClienteResponse();
        response.setContenido(contenidoDto);

        return response;
    }
}
