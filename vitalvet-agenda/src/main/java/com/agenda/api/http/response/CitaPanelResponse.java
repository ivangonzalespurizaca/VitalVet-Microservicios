package com.agenda.api.http.response;

import com.agenda.api.dto.CitaResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CitaPanelResponse extends CitaResponseDTO {
    private String nombreMascota;
    private String razaMascota;
    private String nombrePropietario;
    private String dniPropietario;
    private String nombreMedico;
}
