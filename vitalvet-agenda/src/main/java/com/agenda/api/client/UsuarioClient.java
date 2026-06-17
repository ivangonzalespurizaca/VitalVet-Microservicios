package com.agenda.api.client;

import com.agenda.api.dto.ClienteResponseDTO;
import com.agenda.api.dto.VeterinarioHeaderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MICRO-USUARIO", path = "/api")
public interface UsuarioClient {
    @GetMapping("/cliente/interno/{id}")
    ClienteResponseDTO obtenerDetalleCliente(@PathVariable("id") Long id);

    @GetMapping("/usuario/veterinario/cabecera/{id}")
    VeterinarioHeaderDTO obtenerCabecera(@PathVariable Long id);

}
