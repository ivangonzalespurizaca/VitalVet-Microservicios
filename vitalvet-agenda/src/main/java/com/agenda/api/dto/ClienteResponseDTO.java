package com.agenda.api.dto;

import lombok.Data;

@Data
public class ClienteResponseDTO {
    private Long idPersona;
    private String nombres;
    private String apellidos;
    private String email;
    private String dni;
    private String celular;
    private int totalMascotas;
}