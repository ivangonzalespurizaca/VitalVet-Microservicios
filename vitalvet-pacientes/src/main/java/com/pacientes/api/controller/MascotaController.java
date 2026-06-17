package com.pacientes.api.controller;

import com.pacientes.api.dto.MascotaRequestDTO;
import com.pacientes.api.dto.MascotaResponseDTO;
import com.pacientes.api.entity.Mascota;
import com.pacientes.api.entity.Raza;
import com.pacientes.api.mapper.MascotaMapper;
import com.pacientes.api.services.MascotaService;
import com.pacientes.api.services.RazaService;
import com.pacientes.api.utils.ApiResponse;
import com.pacientes.api.utils.BusinessException;
import com.pacientes.api.utils.ModeloNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/paciente/mascota")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    @Autowired
    private RazaService razaService;

    @Autowired
    private MascotaMapper mascotaMapper;

    @PostMapping("/public/registrar")
    public ResponseEntity<ApiResponse<MascotaResponseDTO>> registrarMascotaPublico(@Valid @RequestBody MascotaRequestDTO dto) throws Exception {

        return procesarRegistroMascota(dto);

    }

    @PostMapping("/registrar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'VETERINARIO')")
    public ResponseEntity<ApiResponse<MascotaResponseDTO>> registrarMascotaPrivado(@Valid @RequestBody MascotaRequestDTO dto) throws Exception {

        return procesarRegistroMascota(dto);

    }

    @GetMapping("/{idMascota}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'CLIENTE')")
    public ResponseEntity<ApiResponse<MascotaResponseDTO>> obtenerPorId(
            @PathVariable("idMascota") Long idMascota) throws Exception {

        Mascota mascota = mascotaService.buscarPorId(idMascota);
        if (mascota == null) {
            throw new ModeloNotFoundException("Mascota con código " + idMascota + " no encontrada");
        }
        MascotaResponseDTO responseDTO = mascotaMapper.toResponseDTO(mascota);

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡Datos de la mascota recuperados con éxito!", responseDTO
        ));
    }

    @GetMapping("/interno/{idMascota}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> obtenerPorIdInterno(
            @PathVariable("idMascota") Long idMascota) throws Exception {

        Mascota mascota = mascotaService.buscarPorId(idMascota);
        if (mascota == null) {
            throw new ModeloNotFoundException("Mascota con código " + idMascota + " no encontrada");
        }
        MascotaResponseDTO responseDTO = mascotaMapper.toResponseDTO(mascota);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/contar/{idCliente}")
    public ResponseEntity<Integer> contarMascotasPorCliente(@PathVariable("idCliente") Long idCliente) {
        int total = mascotaService.contarPorIdCliente(idCliente);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'CLIENTE')")
    public ResponseEntity<ApiResponse<List<MascotaResponseDTO>>> listarPorCliente(@PathVariable("idCliente") Long idCliente) {

        List<Mascota> listaEntidades = mascotaService.listarMascotasPorCliente(idCliente);

        List<MascotaResponseDTO> listaDTO = listaEntidades.stream()
                .map(mascotaMapper::toResponseDTO)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "¡Mascotas del cliente recuperadas con éxito!",
                listaDTO
        ));
    }

    @PutMapping("/editar/{idMascota}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO', 'CLIENTE')")
    public ResponseEntity<ApiResponse<MascotaResponseDTO>> actualizarMascota(
            @PathVariable("idMascota") Long idMascota,
            @Valid @RequestBody MascotaRequestDTO dto) throws Exception{

        Mascota mascotaExistente = mascotaService.buscarPorId(idMascota);

        if(mascotaExistente==null){
            throw new ModeloNotFoundException("Mascota con código " + idMascota+ " no existe.");
        }
        String codigo = mascotaExistente.getCodigoMascota();
        mascotaMapper.updateEntityFromRequestDto(dto, mascotaExistente);
        mascotaExistente.setCodigoMascota(codigo);

        Raza raza = razaService.buscarPorId(dto.getIdRaza());
        if (raza == null) {
            throw new ModeloNotFoundException("La raza especificada no existe.");
        }
        mascotaExistente.setRaza(raza);

        Mascota mascotaActualizada = mascotaService.actualizar(mascotaExistente);
        MascotaResponseDTO responseDTO = mascotaMapper.toResponseDTO(mascotaActualizada);

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡Los datos de la mascota han sido actualizados con éxito!", responseDTO
        ));
    }

    private ResponseEntity<ApiResponse<MascotaResponseDTO>> procesarRegistroMascota(MascotaRequestDTO dto) throws Exception {
        Raza raza = razaService.buscarPorId(dto.getIdRaza());
        if (raza == null) {
            throw new ModeloNotFoundException("La raza especificada no existe.");
        }

        Mascota mascota = mascotaMapper.toEntity(dto);
        mascota.setRaza(raza);

        if (mascota.getPesoActual() == null) {
            mascota.setPesoActual(BigDecimal.ZERO);
        }

        Mascota mascotaGuardada = mascotaService.registrar(mascota);
        MascotaResponseDTO responseDTO = mascotaMapper.toResponseDTO(mascotaGuardada);

        ApiResponse<MascotaResponseDTO> response = new ApiResponse<>(
                true, "¡Mascota registrada exitosamente!", responseDTO
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}