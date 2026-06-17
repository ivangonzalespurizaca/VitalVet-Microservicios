package com.agenda.api.http.response;

import com.agenda.api.dto.ComprobanteDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComprobanteClienteResponse {
    private List<ComprobanteDTO> contenido;
}