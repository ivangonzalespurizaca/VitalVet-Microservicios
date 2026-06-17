package com.agenda.api.services.impl;

import com.agenda.api.client.UsuarioClient;
import com.agenda.api.client.PacienteClient;
import com.agenda.api.dto.*;
import com.agenda.api.entity.Cita;
import com.agenda.api.entity.ComprobantePago;
import com.agenda.api.entity.HorarioAtencion;
import com.agenda.api.entity.enums.DiaSemana;
import com.agenda.api.entity.enums.EstadoCita;
import com.agenda.api.entity.enums.MetodoPago;
import com.agenda.api.entity.enums.TipoComprobante;
import com.agenda.api.http.response.CitaDetalleResponse;
import com.agenda.api.http.response.CitaPanelResponse;
import com.agenda.api.mapper.CitaMapper;
import com.agenda.api.repository.CitaRepository;
import com.agenda.api.repository.ComprobanteRepository;
import com.agenda.api.repository.HorarioAtencionRepository;
import com.agenda.api.services.CitaService;
import com.agenda.api.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl extends ICRUDImpl<Cita, Long> implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorarioAtencionRepository horarioRepository;

    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Autowired
    private CitaMapper citaMapper;

    @Autowired
    private UsuarioClient veterinarioClient;

    @Autowired
    private PacienteClient mascotaClient;

    @Autowired
    private UsuarioClient clienteClient;

    @Override
    public JpaRepository<Cita, Long> getRepository() {
        return citaRepository;
    }

    @Override
    public List<SlotDTO> obtenerHorasDisponibles(Long idVeterinario, LocalDate fecha) {

        int nroDiaSemana = fecha.getDayOfWeek().getValue();
        DiaSemana diaBusqueda = DiaSemana.values()[nroDiaSemana - 1];

        Optional<HorarioAtencion> horarioOpt = horarioRepository.findByIdVeterinarioAndDiaSemanaAndActivoTrue(idVeterinario, diaBusqueda);
        if (horarioOpt.isEmpty()) {
            return Collections.emptyList();
        }

        HorarioAtencion horario = horarioOpt.get();
        List<LocalTime> horasOcupadas = citaRepository.findHorasOcupadasNoCanceladas(idVeterinario, fecha);
        List<SlotDTO> slots = new ArrayList<>();
        LocalTime horaActual = horario.getHoraInicio();
        int intervaloMinutos = 60;
        LocalTime ahora = LocalTime.now();
        LocalDate hoy = LocalDate.now();

        while (horaActual.isBefore(horario.getHoraFin())) {

            boolean estaOcupado = horasOcupadas.contains(horaActual);
            boolean yaPaso = fecha.isBefore(hoy) || (fecha.isEqual(hoy) && horaActual.isBefore(ahora));

            slots.add(SlotDTO.builder()
                    .hora(horaActual)
                    .disponible(!estaOcupado && !yaPaso)
                    .build());
            horaActual = horaActual.plusMinutes(intervaloMinutos);
        }

        return slots;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CitaResponseDTO registrarCita(CitaRequestDTO dto) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if (dto.getFecha().isBefore(hoy) || (dto.getFecha().isEqual(hoy) && dto.getHora().isBefore(ahora))) {
            throw new BusinessException("Operación Inválida: El horario seleccionado ya expiró.");
        }

        long citasPagadasActivas = citaRepository.countByIdMascotaAndEstado(dto.getIdMascota(), EstadoCita.PAGADA);
        if (citasPagadasActivas >= 1) {
            throw new BusinessException("Límite alcanzado: Esta mascota ya cuenta con una cita médica agendada y PAGADA.");
        }

        int nroDiaSemana = dto.getFecha().getDayOfWeek().getValue();
        DiaSemana diaBusqueda = DiaSemana.values()[nroDiaSemana - 1];

        Optional<HorarioAtencion> horarioOpt = horarioRepository.findByIdVeterinarioAndDiaSemanaAndActivoTrue(dto.getIdVeterinario(), diaBusqueda);
        if (horarioOpt.isEmpty()) {
            throw new BusinessException("Operación Denegada: El veterinario seleccionado no cuenta con turnos de atención programados para este día.");
        }

        HorarioAtencion horario = horarioOpt.get();
        if (dto.getHora().isBefore(horario.getHoraInicio()) || !dto.getHora().isBefore(horario.getHoraFin())) {
            throw new BusinessException("Operación Denegada: El veterinario no atiende en el horario seleccionado.");
        }

        boolean slotOcupado = citaRepository.existsByIdVeterinarioAndFechaAndHoraAndEstadoNot(dto.getIdVeterinario(), dto.getFecha(), dto.getHora(), EstadoCita.CANCELADA);
        if (slotOcupado) {
            throw new BusinessException("Conflicto de Agenda: El horario ya fue reservado.");
        }

        Cita nuevaCita = citaMapper.toEntity(dto);
        nuevaCita.setEstado(EstadoCita.PAGADA);
        nuevaCita.setFechaCreacion(LocalDateTime.now());
        nuevaCita.setCodigoCita(generarCodigoUnicoCita(dto.getFecha()));

        Cita citaGuardada = citaRepository.save(nuevaCita);

        generarComprobantePagoInterno(
                citaGuardada,
                dto.getIdCliente(),
                dto.getTipoDocumento(),
                dto.getMetodoPago(),
                dto.getMontoTotal()
        );

        return citaMapper.toResponseDTO(citaGuardada);
    }

    @Override
    public List<CitaPanelResponse> listarCitasPanelPrincipal(EstadoCita estado, String criterio) {
        List<Cita> citasBase = citaRepository.listarCitasConFiltroEstado(estado);

        return citasBase.stream()
                .map(cita -> {
                    CitaPanelResponse dto = citaMapper.toPanelResponseDTO(cita);

                    MascotaResponseDTO mascotaInfo = mascotaClient.obtenerDetalleMascota(cita.getIdMascota());
                    ClienteResponseDTO propietarioInfo = clienteClient.obtenerDetalleCliente(mascotaInfo.getIdCliente());
                    VeterinarioHeaderDTO medicoInfo = veterinarioClient.obtenerCabecera(cita.getIdVeterinario());

                    dto.setNombreMascota(mascotaInfo.getNombreMascota());
                    dto.setRazaMascota(mascotaInfo.getNombreRaza());
                    dto.setNombrePropietario(propietarioInfo.getNombres() + " " +propietarioInfo.getApellidos());
                    dto.setDniPropietario(propietarioInfo.getDni());
                    dto.setNombreMedico(medicoInfo.getNombreCompleto());
                    return dto;
                })
                .filter(dto -> criterio == null || criterio.isBlank() ||
                        dto.getNombreMascota().toLowerCase().contains(criterio.toLowerCase()) ||
                        dto.getNombrePropietario().toLowerCase().contains(criterio.toLowerCase()) ||
                        dto.getDniPropietario().contains(criterio))
                .toList();
    }

    @Override
    public CitaDetalleResponse obtenerDetalleCompletoCita(Long idCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new BusinessException("Operación Inválida: La cita médica solicitada no existe."));

        MascotaResponseDTO mascotaInfo = mascotaClient.obtenerDetalleMascota(cita.getIdMascota());
        ClienteResponseDTO propietarioInfo = clienteClient.obtenerDetalleCliente(mascotaInfo.getIdCliente());
        VeterinarioHeaderDTO medicoInfo = veterinarioClient.obtenerCabecera(cita.getIdVeterinario());

        ConsultaDetalleDTO consultaDto = null;
        try {
            consultaDto = mascotaClient.obtenerConsultaPorCita(idCita);
        } catch (feign.FeignException.NotFound e) {
            consultaDto = null;
        } catch (Exception e) {
            consultaDto = null;
        }
        return CitaDetalleResponse.builder()
                .idCita(cita.getIdCita())
                .codigoCita(cita.getCodigoCita())
                .fecha(cita.getFecha())
                .hora(cita.getHora())
                .motivo(cita.getMotivo())
                .estado(cita.getEstado())
                .idMascota(cita.getIdMascota())
                .nombreMascota(mascotaInfo.getNombreMascota())
                .nombrePropietario(propietarioInfo.getNombres() + " " + propietarioInfo.getApellidos())
                .nombreMedico(medicoInfo.getNombreCompleto())
                .consulta(consultaDto)
                .build();
    }

    private String generarCodigoUnicoCita(LocalDate fecha) {
        Optional<String> ultimoCodigoOpt = citaRepository.findTopCodigoCitaByFechaOrderByCodigoCitaDesc(fecha);
        long siguienteCorrelativo = 1;

        if (ultimoCodigoOpt.isPresent()) {
            String ultimoCodigo = ultimoCodigoOpt.get();
            try {
                String parteNumerica = ultimoCodigo.substring(ultimoCodigo.length() - 3);
                siguienteCorrelativo = Long.parseLong(parteNumerica) + 1;
            } catch (Exception e) {
                siguienteCorrelativo = citaRepository.countByFecha(fecha) + 1;
            }
        }

        String fechaFormato = fecha.toString().replace("-", "");
        return String.format("VV-%s-%03d", fechaFormato, siguienteCorrelativo);
    }

    private void generarComprobantePagoInterno(Cita cita, Long idCliente, String tipoDoc, String metodo, java.math.BigDecimal total) {
        ComprobantePago pago = new ComprobantePago();
        pago.setCita(cita);
        pago.setIdCliente(idCliente);
        pago.setTipoDocumento(TipoComprobante.valueOf(tipoDoc));
        pago.setMetodoPago(MetodoPago.valueOf(metodo));

        java.math.BigDecimal subtotal = total.divide(java.math.BigDecimal.valueOf(1.18), 2, java.math.RoundingMode.HALF_UP);
        pago.setMontoSubtotal(subtotal);
        pago.setMontoImpuesto(total.subtract(subtotal));
        pago.setMontoTotal(total);
        pago.setFechaPago(LocalDateTime.now());
        pago.setCodigoComprobante(generarCodigoCorrelativoComprobante(tipoDoc));

        comprobanteRepository.save(pago);
    }

    private String generarCodigoCorrelativoComprobante(String tipoDoc) {
        String prefijo;

        switch (tipoDoc.toUpperCase()) {
            case "BOLETA":
                prefijo = "B001";
                break;
            case "FACTURA":
                prefijo = "F001";
                break;
            case "RECIBO_INTERNO":
            default:
                prefijo = "R001";
                break;
        }

        Long totalComprobantesTipo = comprobanteRepository.countByTipoDocumento(TipoComprobante.valueOf(tipoDoc));
        Long siguienteCorrelativo = totalComprobantesTipo + 1;
        return String.format("%s-%06d", prefijo, siguienteCorrelativo);
    }

}
