package com.vitalvet.api.controller;

import com.vitalvet.api.dto.AlertaVacunaDTO;
import com.vitalvet.api.dto.CarnetVacunaDTO;
import com.vitalvet.api.dto.VacunaRegistroRequestDTO;
import com.vitalvet.api.dto.VacunaResponseDTO;
import com.vitalvet.api.entity.VacunaAplicada;
import com.vitalvet.api.http.response.MascotasResponse;
import com.vitalvet.api.services.MascotaService;
import com.vitalvet.api.services.VacunaAplicadaService;
import com.vitalvet.api.utils.ApiResponse;
import com.vitalvet.api.utils.BusinessException;
import com.vitalvet.api.utils.ModeloNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paciente/gestion-vacunas")
public class VacunaAplicadaController {

    @Autowired
    private MascotaService mascotaService;

    @Autowired
    private VacunaAplicadaService vacunaAplicadaService;

    @GetMapping("/propietario/dni/{dni}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    public ResponseEntity<ApiResponse<MascotasResponse>> buscarPanelPorDniPropietario(
            @PathVariable("dni") String dni) {

        MascotasResponse data = mascotaService.listarMascotasPorDniCliente(dni.trim());

        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Estructura del panel de vacunación y mascotas cargada con éxito.",
                data
        ), HttpStatus.OK);
    }

    @PostMapping("/mascota/{idMascota}/registrar-manual")
    @PreAuthorize("hasRole('VETERINARIO')")
    public ResponseEntity<ApiResponse<?>> registrarVacunaManual(
            @PathVariable("idMascota") Long idMascota,
            @Valid @RequestBody VacunaRegistroRequestDTO dto) {

        vacunaAplicadaService.registrarVacunaManual(dto, idMascota);

        CarnetVacunaDTO carnetActualizado = vacunaAplicadaService.obtenerCarnetCompletoPorMascota(idMascota, "TODAS");

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "¡El registro del carnet clínico ha sido actualizado exitosamente!",
                carnetActualizado
        ));
    }

    @PutMapping("/vacuna-aplicada/{idAplicacion}/confirmar")
    @PreAuthorize("hasRole('VETERINARIO')")
    public ResponseEntity<ApiResponse<?>> confirmarAplicacionVacuna(
            @PathVariable("idAplicacion") Long idAplicacion,
            @RequestParam(value = "idConsulta", required = false) Long idConsulta) throws Exception {

        VacunaAplicada vacuna = vacunaAplicadaService.buscarPorId(idAplicacion);
        if(vacuna == null){
            throw new ModeloNotFoundException("El registro de vacuna no existe.");
        }

        Long idMascota = vacuna.getMascota().getIdMascota();
        vacunaAplicadaService.confirmarAplicacionVacuna(idAplicacion, idConsulta);
        CarnetVacunaDTO carnetActualizado = vacunaAplicadaService.obtenerCarnetCompletoPorMascota(idMascota, "TODAS");
        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡La vacuna ha sido marcada como APLICADA en el carnet clínico con éxito!", carnetActualizado
        ));
    }

    @GetMapping("/mascota/{idMascota}/carnet")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'CLIENTE')")
    public ResponseEntity<ApiResponse<CarnetVacunaDTO>> obtenerCarnetPorMascota(
            @PathVariable("idMascota") Long idMascota,
            @RequestParam(value = "estado", required = false, defaultValue = "TODAS") String estado) {

        if (!mascotaService.existePorId(idMascota)) {
            throw new ModeloNotFoundException("Mascota con ID " + idMascota + " no encontrada.");
        }
        CarnetVacunaDTO carnetCompleto = vacunaAplicadaService.obtenerCarnetCompletoPorMascota(idMascota, estado);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Carnet de vacunación recuperado con éxito. Filtro: " + estado.toUpperCase(),
                carnetCompleto
        ));
    }

    @GetMapping("/proximas/{idsMascotas}")
    @PreAuthorize("permitAll()") // Asegurado para acceso interno según tu configuración
    public ResponseEntity<List<AlertaVacunaDTO>> obtenerVacunasProximas(
            @PathVariable List<Long> idsMascotas) {

        List<AlertaVacunaDTO> alertas = vacunaAplicadaService.buscarVacunasProximas(idsMascotas);
        return ResponseEntity.ok(alertas);
    }
}