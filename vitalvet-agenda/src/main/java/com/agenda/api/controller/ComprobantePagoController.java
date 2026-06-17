package com.agenda.api.controller;

import com.agenda.api.http.response.ComprobanteAdminResponse;
import com.agenda.api.http.response.ComprobanteClienteResponse;
import com.agenda.api.services.ComprobanteService;
import com.agenda.api.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/agenda/comprobantes")
public class ComprobantePagoController {

    @Autowired
    private ComprobanteService comprobanteService;

    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    public ResponseEntity<ApiResponse<ComprobanteAdminResponse>> consultarHistorial(
            @RequestParam(value = "criterio", required = false) String criterio,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        ComprobanteAdminResponse reporte = comprobanteService.obtenerHistorialComprobantes(criterio, tipo, inicio, fin);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Historial financiero y datos de clientes recuperados exitosamente.",
                reporte
        ));
    }

    @GetMapping("/mis-comprobantes/{idCliente}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ApiResponse<ComprobanteClienteResponse>> obtenerMisComprobantes(
            @PathVariable("idCliente") Long idCliente,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin){

        ComprobanteClienteResponse response = comprobanteService.obtenerComprobantesPorCliente(
                idCliente, tipo, inicio, fin);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Tu historial de comprobantes de pago fue recuperado correctamente.",
                response
        ));
    }
}
