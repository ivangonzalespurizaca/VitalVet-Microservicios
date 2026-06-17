package com.agenda.api.http.response;

import com.agenda.api.dto.ConsultaDetalleDTO;
import com.agenda.api.entity.enums.EstadoCita;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class CitaDetalleResponse {
    private Long idCita;
    private String codigoCita;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoCita estado;
    private Long idMascota;
    private String nombreMascota;
    private String nombrePropietario;
    private String nombreMedico;

    private ConsultaDetalleDTO consulta;
}