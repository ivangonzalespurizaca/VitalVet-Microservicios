package com.agenda.api.client;

import com.agenda.api.dto.ConsultaDetalleDTO;
import com.agenda.api.dto.MascotaResponseDTO;
import com.agenda.api.dto.dashboard.AlertaVacunaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "vitalvet-api", url = "http://localhost:8080")
public interface PacienteClient {

    @GetMapping("/api/paciente/mascota/interno/{idMascota}")
    MascotaResponseDTO obtenerDetalleMascota(@PathVariable("idMascota") Long idMascota);

    @GetMapping("/api/paciente/consulta/cita/interno/{idCita}")
    ConsultaDetalleDTO obtenerConsultaPorCita(@PathVariable("idCita") Long idCita);

    @GetMapping("/api/paciente/mascota/total-activos")
    Long obtenerTotalPacientes();

    @GetMapping("/api/paciente/mascota/interno/cliente/{idCliente}")
    List<MascotaResponseDTO> obtenerMascotasPorCliente(@PathVariable("idCliente") Long idCliente);

    // Obtener vacunas próximas para una lista de mascotas
    @GetMapping("/api/paciente/gestion-vacunas/proximas/{idsMascotas}")
    List<AlertaVacunaDTO> obtenerVacunasProximasPorIds(@PathVariable("idsMascotas") List<Long> idsMascotas);
}