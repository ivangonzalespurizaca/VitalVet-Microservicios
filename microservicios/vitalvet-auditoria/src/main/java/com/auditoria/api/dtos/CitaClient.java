package com.auditoria.api.dtos;


import com.auditoria.api.enums.TipoEstadoCita;
import lombok.Data;

@Data
public class CitaClient {
    Long idUsuario;
    Long idveterinario;
    TipoEstadoCita estadoCita;
}
