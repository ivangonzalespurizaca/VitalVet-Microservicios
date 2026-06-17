package com.agenda.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CitaRequestDTO {

    @NotNull(message = "La mascota seleccionada es obligatoria.")
    private Long idMascota;

    @NotNull(message = "El veterinario es obligatorio.")
    private Long idVeterinario;

    @NotNull(message = "La fecha de la cita es obligatoria.")
    @FutureOrPresent(message = "No se pueden programar citas para días que ya pasaron.")
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria.")
    private LocalTime hora;

    @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres.")
    private String motivo;

    @NotNull(message = "El identificador del cliente es obligatorio.")
    private Long idCliente;

    @NotNull(message = "El tipo de comprobante es obligatorio.")
    private String tipoDocumento;

    @NotNull(message = "El método de pago es obligatorio.")
    private String metodoPago;

    @NotNull(message = "El monto total es obligatorio.")
    private BigDecimal montoTotal;

}