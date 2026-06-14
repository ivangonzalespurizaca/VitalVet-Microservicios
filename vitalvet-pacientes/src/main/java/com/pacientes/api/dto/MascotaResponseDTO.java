package com.pacientes.api.dto;

import com.pacientes.api.entity.enums.SexoMascota;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MascotaResponseDTO {
    private Long idMascota;
    private String codigoMascota;
    private String nombreMascota;
    private SexoMascota sexo;
    private LocalDate fechaNacimiento;
    private BigDecimal pesoActual;
    private String fotoUrl;
    private Long idCliente;
    private String nombreRaza;
}
