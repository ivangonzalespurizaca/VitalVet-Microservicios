package com.usuarios.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VeterinarioHeaderDTO {
    private String dni;
    private String nombreCompleto;
    private String especialidad;
}