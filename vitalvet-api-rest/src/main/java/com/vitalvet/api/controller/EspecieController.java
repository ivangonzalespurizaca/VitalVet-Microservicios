package com.vitalvet.api.controller;

import com.vitalvet.api.dto.EspecieDTO;
import com.vitalvet.api.entity.Especie;
import com.vitalvet.api.services.EspecieService;
import com.vitalvet.api.utils.ApiResponse;
import com.vitalvet.api.utils.BusinessException;
import com.vitalvet.api.utils.ModeloNotFoundException;
import jakarta.validation.Valid;
import org.hibernate.type.TrueFalseConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paciente/especie")
public class EspecieController {

    @Autowired
    private EspecieService service;

    @GetMapping("/listar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<EspecieDTO>>> listarEspecies() throws Exception {
        List<EspecieDTO> lista = service.listar().stream()
                .map(e -> new EspecieDTO(e.getIdEspecie(), e.getNombreEspecie(), e.getActivo()))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Catálogo de especies recuperado.", lista));
    }

    @GetMapping("/listar-activos")
    public ResponseEntity<ApiResponse<List<EspecieDTO>>> listarEspeciesActivas() throws Exception {
        List<EspecieDTO> lista = service.listar().stream().filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .map(e -> new EspecieDTO(e.getIdEspecie(), e.getNombreEspecie(), e.getActivo()))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Catálogo de especies recuperado.", lista));
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<EspecieDTO>> registrar(@Valid @RequestBody EspecieDTO dto) throws Exception {

        if (service.existePorNombre(dto.getNombreEspecie().trim())) {
            throw new BusinessException("Operación Cancelada: Ya existe una especie registrada con el nombre '" + dto.getNombreEspecie().trim() + "'.");
        }

        Especie entidad = new Especie();
        entidad.setNombreEspecie(dto.getNombreEspecie().trim());
        entidad.setActivo(true);

        Especie guardada = service.registrar(entidad);

        EspecieDTO respuestaDTO = EspecieDTO.builder()
                .idEspecie(guardada.getIdEspecie())
                .nombreEspecie(guardada.getNombreEspecie())
                .activo(guardada.getActivo())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "¡Especie registrada con éxito!", respuestaDTO));
    }

    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<EspecieDTO>> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody EspecieDTO dto) throws Exception {

        Especie existente = service.buscarPorId(id);
        if (existente == null) {
            throw new ModeloNotFoundException("La especie solicitada no existe.");
        }

        if (service.existePorNombreYIdDiferente(dto.getNombreEspecie().trim(), id)) {
            throw new BusinessException("Conflicto de Datos: El nombre '" + dto.getNombreEspecie().trim() + "' ya está asignado a otra especie.");
        }

        existente.setNombreEspecie(dto.getNombreEspecie().trim());
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        Especie guardada = service.actualizar(existente);

        dto.setIdEspecie(id);
        dto.setActivo(guardada.getActivo());
        return ResponseEntity.ok(new ApiResponse<>(true, "¡Especie actualizada con éxito!", dto));
    }
}