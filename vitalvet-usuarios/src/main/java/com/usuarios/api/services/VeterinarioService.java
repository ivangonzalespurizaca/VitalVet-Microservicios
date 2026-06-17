package com.usuarios.api.services;

import com.usuarios.api.dto.VeterinarioHeaderDTO;
import com.usuarios.api.dto.VeterinarioRequestDTO;
import com.usuarios.api.entity.Veterinario;

import java.util.List;

public interface VeterinarioService extends ICRUD<Veterinario, Long> {
    List<Veterinario> listarVeterinarios(String criterio);
    List<Veterinario> listarVeterinariosActivos();
    Veterinario registrarVeterinarioCompleto(VeterinarioRequestDTO dto);
    Veterinario actualizarVeterinarioCompleto(Long idVeterinario, VeterinarioRequestDTO dto);
    void desactivarVeterinario(Long idVeterinario);
    void activarVeterinario(Long idVeterinario);
}
