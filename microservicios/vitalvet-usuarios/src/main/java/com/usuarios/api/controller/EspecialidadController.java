package com.usuarios.api.controller;

import com.usuarios.api.dto.EspecialidadDTO;
import com.usuarios.api.entity.Especialidad;
import com.usuarios.api.services.EspecialidadService;
import com.usuarios.api.utils.ApiResponse;
import com.usuarios.api.utils.BusinessException;
import com.usuarios.api.utils.ModeloNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuario/especialidad")
public class EspecialidadController {
    @Autowired
    private EspecialidadService service;

    @GetMapping("/listar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<EspecialidadDTO>>> listarEspecialidades() throws Exception {
        List<EspecialidadDTO> lista = service.listar().stream()
                .map(e -> new EspecialidadDTO(e.getIdEspecialidad(), e.getNombreEspecialidad(), e.getActivo()))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Catálogo de especialidades recuperado.", lista));
    }

    @GetMapping("/listar-activos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'CLIENTE')")
    public ResponseEntity<ApiResponse<List<EspecialidadDTO>>> listarEspecialidadesActivas() throws Exception {
        List<EspecialidadDTO> lista = service.listar().stream().filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .map(e -> new EspecialidadDTO(e.getIdEspecialidad(), e.getNombreEspecialidad(), e.getActivo()))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Catálogo de especialidades recuperado.", lista));
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<EspecialidadDTO>> registrar(@Valid @RequestBody EspecialidadDTO dto) throws Exception {

        if (service.existePorNombre(dto.getNombreEspecialidad().trim())) {
            throw new BusinessException("Operación Cancelada: Ya existe una especialidad registrada con el nombre '" + dto.getNombreEspecialidad().trim() + "'.");
        }

        Especialidad entidad = new Especialidad();
        entidad.setNombreEspecialidad(dto.getNombreEspecialidad().trim());
        entidad.setActivo(true);

        Especialidad guardada = service.registrar(entidad);
        dto.setIdEspecialidad(guardada.getIdEspecialidad());
        dto.setActivo(guardada.getActivo());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "¡Especialidad registrada con éxito!", dto));
    }

    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<EspecialidadDTO>> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody EspecialidadDTO dto) throws Exception {

        Especialidad existente = service.buscarPorId(id);
        if (existente == null) {
            throw new ModeloNotFoundException("La especialidad solicitada no existe.");
        }
        if (service.existePorNombreYIdDiferente(dto.getNombreEspecialidad().trim(), id)) {
            throw new com.usuarios.api.utils.BusinessException("Conflicto de Datos: El nombre '" + dto.getNombreEspecialidad().trim() + "' ya está asignado a otra especialidad.");
        }

        existente.setNombreEspecialidad(dto.getNombreEspecialidad().trim());
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        Especialidad guardada = service.actualizar(existente);

        dto.setIdEspecialidad(id);
        dto.setActivo(guardada.getActivo());
        return ResponseEntity.ok(new ApiResponse<>(true, "¡Especialidad actualizada con éxito!", dto));
    }
}
