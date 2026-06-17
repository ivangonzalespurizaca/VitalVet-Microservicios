package com.agenda.api.services;

import com.agenda.api.dto.*;
import com.agenda.api.entity.Cita;
import com.agenda.api.entity.enums.EstadoCita;
import com.agenda.api.http.response.CitaDetalleResponse;
import com.agenda.api.http.response.CitaPanelResponse;

import java.time.LocalDate;
import java.util.List;

public interface CitaService extends ICRUD<Cita, Long>{

    List<SlotDTO> obtenerHorasDisponibles(Long idVeterinario, LocalDate fecha);
    CitaResponseDTO registrarCita(CitaRequestDTO dto);
    List<CitaPanelResponse> listarCitasPanelPrincipal(EstadoCita estado, String criterio);
    CitaDetalleResponse obtenerDetalleCompletoCita(Long idCita);
}
