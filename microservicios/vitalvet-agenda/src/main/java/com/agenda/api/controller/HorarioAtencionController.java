package com.agenda.api.controller;

import com.agenda.api.client.UsuarioClient;
import com.agenda.api.dto.AgendaBloqueResponseDTO;
import com.agenda.api.dto.HorarioDetalleDTO;
import com.agenda.api.dto.HorarioRequestDTO;
import com.agenda.api.entity.HorarioAtencion;
import com.agenda.api.http.response.HorarioResponse;
import com.agenda.api.kafka.event.AuditoriaEvent;
import com.agenda.api.kafka.producer.AuditoriaProducer;
import com.agenda.api.mapper.HorarioMapper;
import com.agenda.api.record.CitaClient;
import com.agenda.api.record.HorarioR;
import com.agenda.api.services.CitaService;
import com.agenda.api.services.HorarioAtencionService;
import com.agenda.api.utils.ApiResponse;
import com.agenda.api.utils.BusinessException;
import com.agenda.api.utils.ModeloNotFoundException;
import com.agenda.api.utils.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/agenda/horario")
public class HorarioAtencionController {

    @Autowired
    private HorarioAtencionService horarioService;

    @Autowired
    private HorarioMapper horarioMapper;

    @Autowired
    private UsuarioClient veterinarioClient;

    @Autowired
    private CitaService citaService;
    @Autowired
    private AuditoriaProducer producer;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'CLIENTE')")
    @GetMapping("/veterinario/{idVeterinario}")
    public ResponseEntity<ApiResponse<HorarioResponse>> listarPlanificacion(
            @PathVariable("idVeterinario") Long idVeterinario) {

        HorarioResponse data = horarioService.listarPorVet(idVeterinario);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Planificación de horarios del veterinario recuperada con éxito.",
                data
        ));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> registrarHorario(
            @Valid @RequestBody HorarioRequestDTO requestDTO) throws Exception{

        try {
            veterinarioClient.obtenerCabecera(requestDTO.getIdVeterinario());
        } catch (feign.FeignException.NotFound e) {
            throw new ModeloNotFoundException("Operación Cancelada: El veterinario con ID: "
                    + requestDTO.getIdVeterinario() + " no existe en el sistema de Vital Vet.");
        }

        if (!requestDTO.getHoraInicio().isBefore(requestDTO.getHoraFin())) {
            throw new BusinessException("Operación inválida: La hora de inicio debe ser anterior a la hora de fin.");
        }
        boolean existeHorario = horarioService.existePorfechaYVeterinario(
                requestDTO.getIdVeterinario(),
                requestDTO.getDiaSemana()
        );
        if (existeHorario) {
            throw new BusinessException("El veterinario ya tiene un horario planificado para los días " + requestDTO.getDiaSemana());
        }
        HorarioAtencion nuevoHorario = horarioMapper.toEntity(requestDTO);
        nuevoHorario.setDuracionMinutos(60);
        horarioService.registrar(nuevoHorario);
        HorarioDetalleDTO horarioDetalleDTO = horarioMapper.toDetalleDTO(nuevoHorario);

        AuditoriaEvent eventoAuditoria = new AuditoriaEvent(
                "REGISTRO",
                "HORARIO",
                LocalDateTime.now(),
                new HorarioR(horarioDetalleDTO.getIdHorario(),requestDTO.getIdVeterinario())
        );
        producer.enviar(eventoAuditoria);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true, "¡El nuevo turno de atención ha sido registrado con éxito!", horarioDetalleDTO
        ));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{idHorario}")
    public ResponseEntity<ApiResponse<Void>> eliminarHorario(
            @PathVariable("idHorario") Long idHorario) {
        horarioService.eliminarHorarioFisico(idHorario);

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡El horario de atención ha sido removido de la planificación con éxito!", null
        ));
    }

    @GetMapping("/mi-agenda")
    @PreAuthorize("hasRole('VETERINARIO')")
    public ResponseEntity<ApiResponse<List<AgendaBloqueResponseDTO>>> obtenerAgendaDiaria(
            @RequestParam(value = "fecha", required = false, defaultValue = "#{T(java.time.LocalDate).now()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        Long idVeterinario = SecurityUtils.extraerIdVeterinario();
        List<AgendaBloqueResponseDTO> agenda = citaService.obtenerAgendaDiariaVeterinario(idVeterinario, fecha);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Bloques de la agenda diaria del veterinario recuperados con éxito. Fecha: " + fecha,
                agenda
        ));
    }
}