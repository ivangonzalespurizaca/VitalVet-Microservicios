package com.vitalvet.api.controller;

import com.vitalvet.api.dto.VacunaDTO;
import com.vitalvet.api.entity.Vacuna;
import com.vitalvet.api.services.VacunaService;
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
@RequestMapping("/api/paciente/vacuna")
public class VacunaController {

    @Autowired
    private VacunaService service;

    @GetMapping("/listar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<VacunaDTO>>> listarVacunas() throws Exception {
        List<VacunaDTO> lista = service.listar().stream()
                .map(v -> new VacunaDTO(v.getIdVacuna(), v.getNombreVacuna(), v.getDescripcion(), v.getActivo()))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Catálogo de vacunas recuperado con éxito.", lista));
    }

    @GetMapping("/listar-activos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'CLIENTE')")
    public ResponseEntity<ApiResponse<List<VacunaDTO>>> listarVacunasActivas() throws Exception {
        List<VacunaDTO> lista = service.listar().stream().filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .map(v -> new VacunaDTO(v.getIdVacuna(), v.getNombreVacuna(), v.getDescripcion(), v.getActivo()))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Catálogo de vacunas recuperado con éxito.", lista));
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<VacunaDTO>> registrar(@Valid @RequestBody VacunaDTO dto) throws Exception {

        if (service.existePorNombre(dto.getNombreVacuna().trim())) {
            throw new BusinessException("Operación Cancelada: Ya existe una vacuna registrada con el nombre '" + dto.getNombreVacuna().trim() + "'.");
        }

        Vacuna entidad = new Vacuna();
        entidad.setNombreVacuna(dto.getNombreVacuna().trim());
        entidad.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        entidad.setActivo(true);

        Vacuna guardada = service.registrar(entidad);

        VacunaDTO respuestaDTO = VacunaDTO.builder()
                .idVacuna(guardada.getIdVacuna())
                .nombreVacuna(guardada.getNombreVacuna())
                .descripcion(guardada.getDescripcion())
                .activo(guardada.getActivo())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "¡Vacuna registrada con éxito!", respuestaDTO));
    }

    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<VacunaDTO>> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody VacunaDTO dto) throws Exception {

        Vacuna existente = service.buscarPorId(id);
        if (existente == null) {
            throw new ModeloNotFoundException("La vacuna solicitada no existe.");
        }
        if (service.existePorNombreYIdDiferente(dto.getNombreVacuna().trim(), id)) {
            throw new BusinessException("Conflicto de Datos: El nombre '" + dto.getNombreVacuna().trim() + "' ya está asignado a otra vacuna.");
        }

        existente.setNombreVacuna(dto.getNombreVacuna().trim());
        existente.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);

        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        Vacuna guardada = service.actualizar(existente);

        dto.setIdVacuna(id);
        dto.setActivo(guardada.getActivo());
        return ResponseEntity.ok(new ApiResponse<>(true, "¡Vacuna actualizada con éxito!", dto));
    }
}