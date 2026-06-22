package com.vitalvet.api.controller;

import com.vitalvet.api.dto.ConsultaDetalleDTO;
import com.vitalvet.api.dto.ConsultaRequestDTO;
import com.vitalvet.api.dto.ConsultaResumenDTO;
import com.vitalvet.api.entity.Consulta;
import com.vitalvet.api.mapper.ConsultaMapper;
import com.vitalvet.api.services.ConsultaService;
import com.vitalvet.api.services.MascotaService;
import com.vitalvet.api.services.VacunaAplicadaService;
import com.vitalvet.api.utils.ApiResponse;
import com.vitalvet.api.utils.ModeloNotFoundException;
import com.vitalvet.api.utils.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/paciente/consulta")
public class ConsultaController {
    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private VacunaAplicadaService vacunaAplicadaService;

    @Autowired
    private ConsultaMapper consultaMapper;

    @Autowired
    private MascotaService mascotaService;

    @PreAuthorize("permitAll()")
    @GetMapping("/cita/interno/{idCita}")
    public ResponseEntity<ConsultaDetalleDTO> obtenerConsultaPorCita(
            @PathVariable("idCita") Long idCita) {

        Optional<Consulta> consultaOpt = consultaService.buscarPorIdCita(idCita);

        if (consultaOpt.isEmpty()) {
            return ResponseEntity.ok(null);
        }

        Consulta consulta = consultaOpt.get();
        ConsultaDetalleDTO dto = consultaMapper.toDto(consulta);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/mascota/{idMascota}/registrar")
    @PreAuthorize("hasRole('VETERINARIO')")
    public ResponseEntity<?> registrarConsulta(
            @PathVariable("idMascota") Long idMascota,
            @Valid @RequestBody ConsultaRequestDTO dto) {

        dto.setIdVeterinario(SecurityUtils.extraerIdVeterinario());
        Consulta nuevaConsulta = consultaService.registrarConsultaClinica(dto, idMascota);
        ConsultaDetalleDTO responseDTO = consultaMapper.toDto(nuevaConsulta);

        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "¡La consulta médica e historial de vacunación han sido registrados con éxito!",
                responseDTO
        ), HttpStatus.CREATED);
    }
}
