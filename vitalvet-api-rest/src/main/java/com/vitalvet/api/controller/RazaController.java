package com.vitalvet.api.controller;

import com.vitalvet.api.dto.RazaDTO;
import com.vitalvet.api.entity.Especie;
import com.vitalvet.api.entity.Raza;
import com.vitalvet.api.services.EspecieService;
import com.vitalvet.api.services.RazaService;
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
@RequestMapping("/api/paciente/raza")
public class RazaController {

    @Autowired
    private RazaService service;

    @Autowired
    private EspecieService especieService;

    @GetMapping("/listar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<RazaDTO>>> listarRazas() throws Exception {
        List<RazaDTO> lista = service.listar().stream()
                .map(r -> new RazaDTO(
                        r.getIdRaza(),
                        r.getNombreRaza(),
                        r.getEspecie().getIdEspecie(),
                        r.getEspecie().getNombreEspecie(),
                        r.getActivo()))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Catálogo de razas recuperado con éxito.", lista));
    }

    @GetMapping("/listar-activos")
    public ResponseEntity<ApiResponse<List<RazaDTO>>> listarRazasActivas() throws Exception {
        List<RazaDTO> lista = service.listar().stream().filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .map(r -> new RazaDTO(
                        r.getIdRaza(),
                        r.getNombreRaza(),
                        r.getEspecie().getIdEspecie(),
                        r.getEspecie().getNombreEspecie(),
                        r.getActivo()))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Catálogo de razas recuperado con éxito.", lista));
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<RazaDTO>> registrar(@Valid @RequestBody RazaDTO dto) throws Exception {

        Especie especiePadre = especieService.buscarPorId(dto.getIdEspecie());
        if (especiePadre == null) {
            throw new ModeloNotFoundException("Operación Inválida: La especie asociada no existe en el sistema.");
        }

        if (service.existeDuplicado(dto.getNombreRaza().trim(), dto.getIdEspecie())) {
            throw new BusinessException("Conflicto: Ya existe la raza '" + dto.getNombreRaza().trim() + "' en la especie seleccionada.");
        }

        Raza entidad = new Raza();
        entidad.setNombreRaza(dto.getNombreRaza().trim());
        entidad.setEspecie(especiePadre);
        entidad.setActivo(true);

        Raza guardada = service.registrar(entidad);

        RazaDTO respuestaDTO = RazaDTO.builder()
                .idRaza(guardada.getIdRaza())
                .nombreRaza(guardada.getNombreRaza())
                .idEspecie(guardada.getEspecie().getIdEspecie())
                .nombreEspecie(guardada.getEspecie().getNombreEspecie())
                .activo(guardada.getActivo())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "¡Raza registrada con éxito!", respuestaDTO));
    }

    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<RazaDTO>> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody RazaDTO dto) throws Exception {

        Raza existente = service.buscarPorId(id);
        if (existente == null) {
            throw new ModeloNotFoundException("La raza solicitada no existe.");
        }

        Especie especiePadre = especieService.buscarPorId(dto.getIdEspecie());
        if (especiePadre == null) {
            throw new ModeloNotFoundException("La especie asociada no existe.");
        }

        if (service.existeDuplicadoActualizar(dto.getNombreRaza().trim(), dto.getIdEspecie(), id)) {
            throw new BusinessException("Conflicto: El nombre '" + dto.getNombreRaza().trim() + "' ya está registrado en esa especie.");
        }

        existente.setNombreRaza(dto.getNombreRaza().trim());
        existente.setEspecie(especiePadre);

        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }

        Raza guardada = service.actualizar(existente);

        dto.setIdRaza(id);
        dto.setNombreEspecie(guardada.getEspecie().getNombreEspecie());
        dto.setActivo(guardada.getActivo());
        return ResponseEntity.ok(new ApiResponse<>(true, "¡Raza actualizada con éxito!", dto));
    }
}