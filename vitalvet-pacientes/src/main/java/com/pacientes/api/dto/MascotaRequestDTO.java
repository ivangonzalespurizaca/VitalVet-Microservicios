package com.pacientes.api.dto;

import com.pacientes.api.entity.enums.SexoMascota;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MascotaRequestDTO {

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    private String nombreMascota;

    @NotNull(message = "La raza es obligatoria")
    private Long idRaza;

    @NotNull(message = "El sexo es obligatorio")
    private SexoMascota sexo;

    private LocalDate fechaNacimiento;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long idCliente;

    private BigDecimal pesoActual;

    private String fotoUrl;
}