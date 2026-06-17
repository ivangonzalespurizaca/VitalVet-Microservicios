package com.agenda.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MascotaResponseDTO {
    private Long idMascota;
    private String codigoMascota;
    private String nombreMascota;
    private LocalDate fechaNacimiento;
    private BigDecimal pesoActual;
    private String fotoUrl;
    private Long idCliente;

    private Long idRaza;
    private String nombreRaza;

    private Long idEspecie;
    private String nombreEspecie;
    private boolean activo;
}