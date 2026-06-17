package com.agenda.api.http.response;

import com.agenda.api.dto.HorarioDetalleDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HorarioResponse {
    private String dni;
    private String nombreCompleto;
    private String especialidad;

    private List<HorarioDetalleDTO> horarios;
}
