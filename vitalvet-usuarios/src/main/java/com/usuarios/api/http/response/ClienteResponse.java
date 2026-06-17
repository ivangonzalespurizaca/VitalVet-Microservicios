package com.usuarios.api.http.response;

import lombok.Data;

@Data
public class ClienteResponse {
    private Long idPersona;
    private String nombres;
    private String apellidos;
    private String email;
    private String dni;
    private String celular;
    private int totalMascotas;
}
