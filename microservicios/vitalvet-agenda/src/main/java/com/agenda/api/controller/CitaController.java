package com.agenda.api.controller;

import com.agenda.api.dto.*;
import com.agenda.api.entity.enums.TipoEstadoCita;
import com.agenda.api.http.response.CitaDetalleResponse;
import com.agenda.api.http.response.CitaPanelResponse;
import com.agenda.api.kafka.event.AuditoriaEvent;
import com.agenda.api.kafka.producer.AuditoriaProducer;
import com.agenda.api.record.CitaClient;
import com.agenda.api.services.CitaService;
import com.agenda.api.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agenda/cita")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private AuditoriaProducer producer;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'CLIENTE')")
    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<List<SlotDTO>>> listarSlotsDisponibles(
            @RequestParam("idVeterinario") Long idVeterinario,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<SlotDTO> slots = citaService.obtenerHorasDisponibles(idVeterinario, fecha);

        String mensaje = slots.isEmpty()
                ? "El veterinario no cuenta con planificación de turnos asignada para este día."
                : "Bloques de horarios disponibles recuperados con éxito para la fecha seleccionada.";

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                mensaje,
                slots
        ));
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CLIENTE')")
    public ResponseEntity<ApiResponse<CitaResponseDTO>> registrarCita(
            @Valid @RequestBody CitaRequestDTO dto) {
        CitaResponseDTO responseDTO = citaService.registrarCita(dto);
        AuditoriaEvent eventoAuditoria = new AuditoriaEvent(
                "REGISTRO",
                "CITAS",
                LocalDateTime.now(),
                new CitaClient(responseDTO.getIdCita(),responseDTO.getIdVeterinario(),responseDTO.getEstado())
        );
        producer.enviar(eventoAuditoria);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "¡La cita médica ha sido agendada y su comprobante de pago fue emitido con éxito!",
                responseDTO
        ));
    }

    @GetMapping("/panel")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'CLIENTE')")
    public ResponseEntity<ApiResponse<List<CitaPanelResponse>>> obtenerPanelCitas(
            @RequestParam(value = "estado", required = false) TipoEstadoCita estado,
            @RequestParam(value = "buscar", required = false) String buscar,
            Authentication authentication) {

        Long idCliente = null;
        Long idVeterinario = null;

        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_CLIENTE"));

        boolean esVeterinario = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_VETERINARIO"));

        if (authentication.getDetails() instanceof Map<?, ?> claims) {
            if (esCliente) {
                Object idPersonaObj = claims.containsKey("idPersona") ? claims.get("idPersona") : claims.get("idpersona");
                if (idPersonaObj != null) idCliente = Long.valueOf(idPersonaObj.toString());

            } else if (esVeterinario) {
                Object idVetObj = claims.containsKey("idVeterinario") ? claims.get("idVeterinario") : claims.get("idveterinario");
                if (idVetObj != null) idVeterinario = Long.valueOf(idVetObj.toString());
            }
        }

        List<CitaPanelResponse> panel = citaService.listarCitasPanelPrincipal(estado, buscar, idCliente, idVeterinario);
        return ResponseEntity.ok(new ApiResponse<>(
                true, "Estructura del panel de citas médicas cargada con éxito.", panel
        ));
    }

    @GetMapping("/detalle/{idCita}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'CLIENTE')")
    public ResponseEntity<ApiResponse<?>> obtenerDetalleCita(
            @PathVariable("idCita") Long idCita) {

        CitaDetalleResponse detalle = citaService.obtenerDetalleCompletoCita(idCita);

        String mensaje = (detalle.getConsulta() == null)
                ? "Cita operativa recuperada. La mascota aún no ha ingresado a consulta médica."
                : "Expediente médico y operativo recuperado exitosamente.";

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                mensaje,
                detalle
        ));
    }

    @PutMapping("/interno/{idCita}/completar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> completarCitaInterno(@PathVariable("idCita") Long idCita) {
        citaService.cambiarEstadoCompletadoInterno(idCita);
        return ResponseEntity.ok().build();
    }
}