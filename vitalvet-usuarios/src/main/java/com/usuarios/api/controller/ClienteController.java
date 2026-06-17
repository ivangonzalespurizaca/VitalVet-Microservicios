package com.usuarios.api.controller;

import com.usuarios.api.client.PacienteClient;
import com.usuarios.api.dto.*;
import com.usuarios.api.entity.Persona;
import com.usuarios.api.http.response.ClienteResponse;
import com.usuarios.api.mapper.PersonaMapper;
import com.usuarios.api.services.AuthService;
import com.usuarios.api.services.PersonaService;
import com.usuarios.api.utils.ApiResponse;
import com.usuarios.api.utils.BusinessException;
import com.usuarios.api.utils.ModeloNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cliente")
public class ClienteController {

    @Autowired
    private PersonaService personaService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PersonaMapper personaMapper;

    @Autowired
    private PacienteClient pacienteClient;

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listarClientes(
            @RequestParam(value = "criterio", required = false) String criterio) {

        List<ClienteResponse> listaDTO = personaService.buscarClientesPorFiltro(criterio).stream()
                .map(persona -> {
                    ClienteResponse dto = personaMapper.toClienteResponseDTO(persona);
                    dto.setTotalMascotas(personaService.obtenerTotalMascotas(persona.getIdPersona()));
                    return dto;
                }).toList();

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡Listado de clientes/dueños procesado con éxito!", listaDTO
        ));
    }

    @PostMapping("/registro-rapido")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ApiResponse<ClienteResponse>> registroRapidoMostrador(@Valid @RequestBody RegistroRapidoDTO request) throws Exception {

        Persona clienteGuardado = authService.registrarCliente(request.getDatosCliente());
        ClienteResponse clienteResponseDTO = personaMapper.toClienteResponseDTO(clienteGuardado);

        MascotaRequestDTO mascotaDTO = request.getDatosMascota();
        mascotaDTO.setIdCliente(clienteGuardado.getIdPersona());

        try {
            pacienteClient.registrarMascotaPublicoInterno(mascotaDTO);
            clienteResponseDTO.setTotalMascotas(1);
        } catch (Exception e) {
            throw new RuntimeException("Error al registrar la mascota en mostrador. Proceso revertido: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true, "¡Propietario y mascota registrados en mostrador con éxito!", clienteResponseDTO
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorId(
            @PathVariable("id") Long id) throws Exception {

        Persona cliente = personaService.buscarPorId(id);
        if (cliente == null) {
            throw new ModeloNotFoundException("Cliente con código " + id + " no encontrado");
        }

        ClienteResponse responseDTO = personaMapper.toClienteResponseDTO(cliente);
        responseDTO.setTotalMascotas(personaService.obtenerTotalMascotas(id));

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡Datos del cliente recuperados con éxito!", responseDTO
        ));
    }

    @GetMapping("/interno/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> obtenerPorIdInterno(
            @PathVariable("id") Long id) throws Exception {

        Persona cliente = personaService.buscarPorId(id);
        if (cliente == null) {
            throw new ModeloNotFoundException("Cliente con código " + id + " no encontrado");
        }

        ClienteResponse responseDTO = personaMapper.toClienteResponseDTO(cliente);
        responseDTO.setTotalMascotas(personaService.obtenerTotalMascotas(id));

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    public ResponseEntity<ApiResponse<?>> editarCliente(@PathVariable("id") Long id, @Valid @RequestBody PersonaRequestDTO dto) throws Exception{

        Persona personaExistente = personaService.buscarPorId(id);
        if (personaExistente == null) {
            throw new BusinessException("El cliente con el ID " + id + " no existe.");
        }

        if (!personaExistente.getDni().trim().equals(dto.getDni().trim())) {
            if (personaService.existeDni(dto.getDni().trim())) {
                throw new BusinessException("No se puede actualizar: El número de DNI '" + dto.getDni().trim() + "' ya pertenece a otro usuario.");
            }
        }

        personaMapper.updatePersonaFromRequestDTO(dto, personaExistente);

        Persona personaActualizada = personaService.actualizar(personaExistente);
        PerfilResponseDTO responseDTO = personaMapper.toResponseDTO(personaActualizada, personaActualizada.getUsuario());

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡Datos del cliente actualizados con éxito!", responseDTO
        ));
    }
}