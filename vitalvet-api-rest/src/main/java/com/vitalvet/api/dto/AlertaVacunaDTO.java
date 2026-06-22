package com.vitalvet.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor  // <--- ¡ESTO ES LO QUE FALTA!
@AllArgsConstructor
public class AlertaVacunaDTO {
    private String nombreMascota;
    private String nombreVacuna;
    private LocalDate fechaVencimiento;
    private String nivelAlerta;
}