package com.agenda.api.controller;

import com.agenda.api.dto.dashboard.AdminDashboardDTO;
import com.agenda.api.dto.dashboard.ClienteDashboardDTO;
import com.agenda.api.dto.dashboard.VeterinarioDashboardDTO;
import com.agenda.api.services.DashboardService;
import com.agenda.api.utils.ApiResponse;
import com.agenda.api.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agenda/dashboard")
@RequiredArgsConstructor // Crea automáticamente el constructor para la inyección
public class DashboardController {

    private final DashboardService dashboardService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getDashboardAdmin() {
        AdminDashboardDTO data = dashboardService.obtenerDatosAdmin();
        return ResponseEntity.ok(new ApiResponse<>(true, "Datos del Dashboard cargados exitosamente", data));
    }

    @PreAuthorize("hasRole('VETERINARIO')")
    @GetMapping("/veterinario")
    public ResponseEntity<VeterinarioDashboardDTO> getDashboardVeterinario() {

        Long idVeterinario = SecurityUtils.extraerIdVeterinario();

        VeterinarioDashboardDTO data = dashboardService.obtenerDatosVeterinario(idVeterinario);

        return ResponseEntity.ok(data);
    }

    // Endpoint para el panel de cliente
    @GetMapping("/cliente")
    @PreAuthorize("hasRole('CLIENTE')") // O hasAnyRole('CLIENTE', 'ADMINISTRADOR')
    public ResponseEntity<ApiResponse<ClienteDashboardDTO>> obtenerDatosCliente() {

        Long idCliente = SecurityUtils.extraerIdCliente();

        ClienteDashboardDTO datos = dashboardService.obtenerDatosCliente(idCliente);



        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Panel de cliente cargado correctamente.",
                datos
        ));
    }

}