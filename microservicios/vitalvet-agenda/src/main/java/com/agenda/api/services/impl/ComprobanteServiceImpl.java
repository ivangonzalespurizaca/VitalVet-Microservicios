package com.agenda.api.services.impl;

import com.agenda.api.client.PacienteClient;
import com.agenda.api.client.UsuarioClient;
import com.agenda.api.dto.ClienteResponseDTO;
import com.agenda.api.dto.ComprobanteDTO;
import com.agenda.api.dto.MascotaResponseDTO;
import com.agenda.api.entity.Cita;
import com.agenda.api.entity.ComprobantePago;
import com.agenda.api.entity.enums.TipoComprobante;
import com.agenda.api.http.response.ComprobanteAdminResponse;
import com.agenda.api.http.response.ComprobanteClienteResponse;
import com.agenda.api.repository.CitaRepository;
import com.agenda.api.repository.ComprobanteRepository;
import com.agenda.api.services.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComprobanteServiceImpl implements ComprobanteService {
    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private UsuarioClient usuarioClient;

    @Autowired
    private PacienteClient pacienteClient;

    @Override
    public ComprobanteAdminResponse obtenerHistorialComprobantes(String tipo, LocalDate inicio, LocalDate fin) {
        TipoComprobante tipoEnum = (tipo != null && !tipo.isEmpty()) ? TipoComprobante.valueOf(tipo) : null;

        List<ComprobantePago> resultados = comprobanteRepository.listarComprobantesConFiltros(
                null, tipoEnum, toStartOfDay(inicio), toEndOfDay(fin));

        long totalEmitidos = resultados.size();
        BigDecimal totalRecaudado = resultados.stream()
                .map(ComprobantePago::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ComprobanteDTO> contenidoDto = resultados.stream().map(c -> {
            String nombreCompleto = "Cliente ID: " + c.getIdCliente();
            String dni = "";
            Long idMascota = null;
            Long idCita = null;
            String codigoCita = "";
            String motivo = "";
            String nombreMascota = "";
            String especieMascota = "";
            String razaMascota = "";

            try {
                ClienteResponseDTO cliente = usuarioClient.obtenerDetalleCliente(c.getIdCliente());
                if (cliente != null) {
                    nombreCompleto = cliente.getNombres() + " " + cliente.getApellidos();
                    dni = cliente.getDni();
                }
                if(c.getCita() != null){
                    idMascota = c.getCita().getIdMascota();
                    codigoCita = c.getCita().getCodigoCita();
                    motivo = c.getCita().getMotivo();

                    MascotaResponseDTO mascota =
                            pacienteClient.obtenerDetalleMascota(idMascota);
                    idCita = c.getCita().getIdCita();
                    if (mascota != null) {
                        nombreMascota = mascota.getNombreMascota();
                        especieMascota = mascota.getNombreEspecie();
                        razaMascota = mascota.getNombreRaza();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ComprobanteDTO.builder()
                    .idComprobante(c.getIdComprobante())
                    .tipoComprobante(c.getTipoDocumento().name())
                    .codigoComprobante(c.getCodigoComprobante())
                    .fechaPago(c.getFechaPago())
                    .metodoPago(c.getMetodoPago().name())
                    .montoSubtotal(c.getMontoSubtotal())
                    .montoImpuesto(c.getMontoImpuesto())
                    .montoTotal(c.getMontoTotal())
                    .nombreCliente(nombreCompleto)
                    .dniCliente(dni)
                    .idMascota(idMascota)
                    .nombreMascota(nombreMascota)
                    .codigoCita(codigoCita)
                    .idCita(idCita)
                    .motivo(motivo)
                    .especieMascota(especieMascota)
                    .razaMascota(razaMascota)
                    .build();
        }).toList();

        ComprobanteAdminResponse response = new ComprobanteAdminResponse();
        response.setContenido(contenidoDto);
        response.setTotalComprobantesEmitidos(totalEmitidos);
        response.setMontoTotalRecaudado(totalRecaudado);

        return response;
    }

    @Override
    public ComprobanteClienteResponse obtenerComprobantesPorCliente(
            Long idCliente, String tipo, LocalDate inicio, LocalDate fin) {

        TipoComprobante tipoEnum = (tipo != null && !tipo.isEmpty())
                ? TipoComprobante.valueOf(tipo.toUpperCase().trim())
                : null;

        List<ComprobantePago> resultados = comprobanteRepository.listarComprobantesConFiltros(
                idCliente, tipoEnum, toStartOfDay(inicio), toEndOfDay(fin));

        List<ComprobanteDTO> contenidoDto = resultados.stream().map(c -> {

            String nombreCompleto = "Cliente ID: " + idCliente;
            String dni = "";
            Long idMascota = null;
            Long idCita = null;
            String codigoCita = "";
            String motivo = "";
            String nombreMascota = "";
            String especieMascota = "";
            String razaMascota = "";

            try {
                ClienteResponseDTO cliente = usuarioClient.obtenerDetalleCliente(idCliente);
                if (cliente != null) {
                    nombreCompleto = cliente.getNombres() + " " + cliente.getApellidos();
                    dni = cliente.getDni();
                }

                if (c.getCita() != null) {
                    idMascota = c.getCita().getIdMascota();
                    idCita = c.getCita().getIdCita();
                    codigoCita = c.getCita().getCodigoCita();
                    motivo = c.getCita().getMotivo();

                    MascotaResponseDTO mascota = pacienteClient.obtenerDetalleMascota(idMascota);

                    if (mascota != null) {
                        nombreMascota = mascota.getNombreMascota();
                        especieMascota = mascota.getNombreEspecie();
                        razaMascota = mascota.getNombreRaza();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return ComprobanteDTO.builder()
                    .idComprobante(c.getIdComprobante())
                    .tipoComprobante(c.getTipoDocumento().name())
                    .codigoComprobante(c.getCodigoComprobante())
                    .fechaPago(c.getFechaPago())
                    .metodoPago(c.getMetodoPago().name())
                    .montoSubtotal(c.getMontoSubtotal())
                    .montoImpuesto(c.getMontoImpuesto())
                    .montoTotal(c.getMontoTotal())
                    .nombreCliente(nombreCompleto)
                    .dniCliente(dni)
                    .idMascota(idMascota)
                    .nombreMascota(nombreMascota)
                    .codigoCita(codigoCita)
                    .idCita(idCita)
                    .motivo(motivo)
                    .especieMascota(especieMascota)
                    .razaMascota(razaMascota)
                    .build();

        }).toList();

        ComprobanteClienteResponse response = new ComprobanteClienteResponse();
        response.setContenido(contenidoDto);

        return response;
    }

    private LocalDateTime toStartOfDay(LocalDate date) {
        return (date != null) ? date.atStartOfDay() : null;
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return (date != null) ? date.atTime(23, 59, 59) : null;
    }
}
