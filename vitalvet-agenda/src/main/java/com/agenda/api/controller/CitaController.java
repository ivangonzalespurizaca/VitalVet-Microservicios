package com.agenda.api.controller;

import com.agenda.api.dto.*;
import com.agenda.api.entity.enums.EstadoCita;
import com.agenda.api.http.response.CitaDetalleResponse;
import com.agenda.api.http.response.CitaPanelResponse;
import com.agenda.api.services.CitaService;
import com.agenda.api.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agenda/cita")
public class CitaController {

    @Autowired
    private CitaService citaService;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "¡La cita médica ha sido agendada y su comprobante de pago fue emitido con éxito!",
                responseDTO
        ));
    }

    @GetMapping("/panel")
    public ResponseEntity<ApiResponse<List<CitaPanelResponse>>> obtenerPanelCitas(
            @RequestParam(value = "estado", required = false) EstadoCita estado,
            @RequestParam(value = "buscar", required = false) String buscar) {

        List<CitaPanelResponse> panel = citaService.listarCitasPanelPrincipal(estado, buscar);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Estructura del panel de citas médicas cargada con éxito.",
                panel
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
}