package com.agenda.api.client;

import com.agenda.api.dto.ConsultaDetalleDTO;
import com.agenda.api.dto.MascotaResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MICRO-PACIENTE")
public interface PacienteClient {

    @GetMapping("/api/paciente/mascota/interno/{idMascota}")
    MascotaResponseDTO obtenerDetalleMascota(@PathVariable("idMascota") Long idMascota);

    @GetMapping("/api/paciente/consulta/cita/interno/{idCita}")
    ConsultaDetalleDTO obtenerConsultaPorCita(@PathVariable("idCita") Long idCita);
}