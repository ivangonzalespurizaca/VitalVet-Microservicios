package com.usuarios.api.controller;

import com.usuarios.api.dto.VeterinarioRequestDTO;
import com.usuarios.api.dto.VeterinarioResponseDTO;
import com.usuarios.api.entity.Veterinario;
import com.usuarios.api.mapper.VeterinarioMapper;
import com.usuarios.api.services.VeterinarioService;
import com.usuarios.api.utils.ApiResponse;
import com.usuarios.api.utils.ModeloNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndViewDefiningException;

import java.util.List;

@RestController
@RequestMapping("/api/usuario/veterinario")
public class VeterinarioController {
    @Autowired
    private VeterinarioService vService;

    @Autowired
    private VeterinarioMapper vMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    public ResponseEntity<ApiResponse<List<VeterinarioResponseDTO>>> listar(
            @RequestParam(value = "buscar", required = false, defaultValue = "") String buscar) {

        List<Veterinario> entidades = vService.listarVeterinarios(buscar);

        List<VeterinarioResponseDTO> listaDTO = entidades.stream()
                .map(vMapper::toResponseDTO)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Listado de veterinarios recuperado", listaDTO));
    }

    @GetMapping("/{idVeterinario}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    public ResponseEntity<ApiResponse<VeterinarioResponseDTO>> obtenerPorId(
            @PathVariable("idVeterinario") Long idVeterinario) throws Exception{

        Veterinario veterinario = vService.buscarPorId(idVeterinario);
        if(veterinario==null){
            throw new ModeloNotFoundException("Veterinario con codigo "+ idVeterinario + " no encontrado");
        }

        VeterinarioResponseDTO responseDTO = vMapper.toResponseDTO(veterinario);

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡Datos del veterinario recuperados con éxito!", responseDTO
        ));
    }

    @GetMapping("/activos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<VeterinarioResponseDTO>>> listarActivos() {

        List<Veterinario> entidades = vService.listarVeterinariosActivos();

        List<VeterinarioResponseDTO> listaDTO = entidades.stream()
                .map(vMapper::toResponseDTO)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Veterinarios activos recuperados", listaDTO));
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> registrar(@Valid @RequestBody VeterinarioRequestDTO dto) throws Exception {

        Veterinario nuevoVeterinario = vService.registrarVeterinarioCompleto(dto);
        VeterinarioResponseDTO responseDTO = vMapper.toResponseDTO(nuevoVeterinario);

        return new ResponseEntity<>(new ApiResponse<>(
                true, "¡El veterinario y sus credenciales de acceso se han registrado con éxito!", responseDTO
        ), HttpStatus.CREATED);
    }

    @PutMapping("/editar/{idVeterinario}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> actualizar(
            @PathVariable("idVeterinario") Long idVeterinario,
            @Valid @RequestBody VeterinarioRequestDTO dto) throws Exception {

        Veterinario veterinarioActualizado = vService.actualizarVeterinarioCompleto(idVeterinario, dto);
        VeterinarioResponseDTO responseDTO = vMapper.toResponseDTO(veterinarioActualizado);

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡Los datos del veterinario han sido actualizados con éxito!", responseDTO
        ));
    }

    @DeleteMapping("/desactivar/{idVeterinario}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> desactivarVeterinario(
            @PathVariable("idVeterinario") Long idVeterinario) throws Exception {

        vService.desactivarVeterinario(idVeterinario);

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡La cuenta del veterinario ha sido desactivada con éxito!", null
        ));
    }

    @PatchMapping("/activar/{idVeterinario}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> activarVeterinario(
            @PathVariable("idVeterinario") Long idVeterinario) throws Exception {

        vService.activarVeterinario(idVeterinario);

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡La cuenta del veterinario ha sido reactivada con éxito!", null
        ));
    }

    @GetMapping("/cabecera/{id}")
    public ResponseEntity<?> obtenerCabecera(@PathVariable Long id) throws Exception{
        Veterinario veterinario = vService.buscarPorId(id);
        if(veterinario==null){
            throw new ModeloNotFoundException("El veterinario con ID: " + id + " no existe");
        }
        return ResponseEntity.ok(vMapper.toHeaderDTO(veterinario));
    }
}
