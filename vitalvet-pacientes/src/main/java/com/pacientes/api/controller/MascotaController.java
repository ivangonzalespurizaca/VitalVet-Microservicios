package com.pacientes.api.controller;

import com.pacientes.api.dto.MascotaRequestDTO;
import com.pacientes.api.dto.MascotaResponseDTO;
import com.pacientes.api.entity.Mascota;
import com.pacientes.api.entity.Raza;
import com.pacientes.api.mapper.MascotaMapper;
import com.pacientes.api.services.MascotaService;
import com.pacientes.api.services.RazaService;
import com.pacientes.api.utils.ApiResponse;
import com.pacientes.api.utils.ModeloNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ApiResponse<MascotaResponseDTO>> registrarMascotaPrivado(@Valid @RequestBody MascotaRequestDTO dto) throws Exception {
        return procesarRegistroMascota(dto);
    }

    private ResponseEntity<ApiResponse<MascotaResponseDTO>> procesarRegistroMascota(MascotaRequestDTO dto) throws Exception {
        Raza raza = razaService.buscarPorId(dto.getIdRaza());
        if (raza == null) {
            throw new ModeloNotFoundException("La raza especificada no existe.");
        }

        Mascota mascota = mascotaMapper.toEntity(dto);
        mascota.setRaza(raza);

        Mascota mascotaGuardada = mascotaService.registrar(mascota);
        MascotaResponseDTO responseDTO = mascotaMapper.toResponseDTO(mascotaGuardada);

        ApiResponse<MascotaResponseDTO> response = new ApiResponse<>(
                true,
                "¡Mascota registrada exitosamente!",
                responseDTO
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}