package com.agenda.api.services.impl;

import com.agenda.api.client.UsuarioClient;
import com.agenda.api.client.PacienteClient;
import com.agenda.api.dto.*;
import com.agenda.api.entity.Cita;
import com.agenda.api.entity.ComprobantePago;
import com.agenda.api.entity.HorarioAtencion;
import com.agenda.api.entity.enums.DiaSemana;
import com.agenda.api.entity.enums.TipoEstadoCita;
import com.agenda.api.entity.enums.TipoMetodoPago;
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

        try {
            MascotaResponseDTO mascota = mascotaClient.obtenerDetalleMascota(dto.getIdMascota());
            if (mascota == null) {
                throw new BusinessException("Operación Inválida: La mascota con el ID especificado no está registrada.");
            }
        } catch (feign.FeignException.NotFound e) {
            throw new BusinessException("Operación Inválida: La mascota con ID " + dto.getIdMascota() + " no existe en el sistema de pacientes.");
        }

        if (dto.getFecha().isBefore(hoy) || (dto.getFecha().isEqual(hoy) && dto.getHora().isBefore(ahora))) {
            throw new BusinessException("Operación Inválida: El horario seleccionado ya expiró.");
        }

        long citasPagadasActivas = citaRepository.countByIdMascotaAndEstado(dto.getIdMascota(), TipoEstadoCita.PAGADA);
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

        boolean slotOcupado = citaRepository.existsByIdVeterinarioAndFechaAndHoraAndEstadoNot(dto.getIdVeterinario(), dto.getFecha(), dto.getHora(), TipoEstadoCita.CANCELADA);
        if (slotOcupado) {
            throw new BusinessException("Conflicto de Agenda: El horario ya fue reservado.");
        }

        Cita nuevaCita = citaMapper.toEntity(dto);
        nuevaCita.setEstado(TipoEstadoCita.PAGADA);
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
    public List<CitaPanelResponse> listarCitasPanelPrincipal(TipoEstadoCita estado, String criterio, Long idCliente, Long idVeterinario) {

        List<Cita> citasBase = (estado == null)
                ? citaRepository.findAllByOrderByFechaDescHoraAsc()
                : citaRepository.findByEstadoOrderByFechaDescHoraAsc(estado);

        return citasBase.stream()
                .map(cita -> {
                    CitaPanelResponse dto = citaMapper.toPanelResponseDTO(cita);
                    try {
                        MascotaResponseDTO mascotaInfo = mascotaClient.obtenerDetalleMascota(cita.getIdMascota());
                        ClienteResponseDTO propietarioInfo = clienteClient.obtenerDetalleCliente(mascotaInfo.getIdCliente());
                        VeterinarioHeaderDTO medicoInfo = veterinarioClient.obtenerCabecera(cita.getIdVeterinario());

                        dto.setNombreMascota(mascotaInfo.getNombreMascota());
                        dto.setRazaMascota(mascotaInfo.getNombreRaza());
                        dto.setNombrePropietario(propietarioInfo.getNombres() + " " + propietarioInfo.getApellidos());
                        dto.setDniPropietario(propietarioInfo.getDni());
                        dto.setNombreMedico(medicoInfo.getNombreCompleto());
                        dto.setIdCliente(propietarioInfo.getIdPersona());
                        dto.setIdVeterinario(medicoInfo.getIdVeterinario());
                    } catch (Exception e) {
                        System.err.println("Error de sincronización Feign para la cita ID: " + cita.getIdCita());
                    }
                    return dto;
                })
                .filter(dto -> idCliente == null || (dto.getIdCliente() != null && dto.getIdCliente().equals(idCliente)))
                .filter(dto -> criterio == null || criterio.isBlank() ||
                        (dto.getNombreMascota() != null && dto.getNombreMascota().toLowerCase().contains(criterio.toLowerCase())) ||
                        (dto.getNombrePropietario() != null && dto.getNombrePropietario().toLowerCase().contains(criterio.toLowerCase())) ||
                        (dto.getDniPropietario() != null && dto.getDniPropietario().contains(criterio)))
                .filter(dto -> idVeterinario == null || (dto.getIdVeterinario() != null && dto.getIdVeterinario().equals(idVeterinario)))
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

    @Override
    @Transactional
    public void cambiarEstadoCompletadoInterno(Long idCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new BusinessException("Error Interno: No se encontró la cita con ID " + idCita + " para actualizar su estado."));
        cita.setEstado(TipoEstadoCita.COMPLETADA);
        citaRepository.save(cita);
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
        pago.setMetodoPago(TipoMetodoPago.valueOf(metodo));

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

    @Override
    public List<AgendaBloqueResponseDTO> obtenerAgendaDiariaVeterinario(Long idVeterinario, LocalDate fecha) {
        List<SlotDTO> slotsBase = this.obtenerHorasDisponibles(idVeterinario, fecha);
        List<CitaPanelResponse> citasMapeadas = this.listarCitasPanelPrincipal(null, null, null, idVeterinario)
                .stream()
                .filter(c -> c.getFecha().isEqual(fecha))
                .toList();

        return slotsBase.stream().map(slot -> {

            Optional<CitaPanelResponse> citaOcupada = citasMapeadas.stream()
                    .filter(c -> c.getHora().equals(slot.getHora()))
                    .findFirst();

            if (citaOcupada.isPresent()) {
                CitaPanelResponse infoCita = citaOcupada.get();

                String horaFormato = slot.getHora().toString() + (slot.getHora().getHour() < 12 ? " AM" : " PM");

                return AgendaBloqueResponseDTO.builder()
                        .hora(horaFormato)
                        .disponible(false)
                        .idCita(infoCita.getIdCita())
                        .nombreMascota(infoCita.getNombreMascota())
                        .dniPropietario(infoCita.getDniPropietario())
                        .razaMascota(infoCita.getRazaMascota())
                        .nombrePropietario(infoCita.getNombrePropietario())
                        .motivoConsulta(infoCita.getMotivo())
                        .build();
            } else {
                String horaFormato = slot.getHora().toString() + (slot.getHora().getHour() < 12 ? " AM" : " PM");

                return AgendaBloqueResponseDTO.builder()
                        .hora(horaFormato)
                        .disponible(true)
                        .build();
            }
        }).toList();
    }

}
