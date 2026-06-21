package com.auditoria.api.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaEvent {

    private String accion;
    private String modulo;
    private Long idUsuario;
    private String descripcion;
    private LocalDateTime fecha;
}