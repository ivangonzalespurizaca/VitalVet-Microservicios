package com.usuarios.api.kafka.event;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class AuditoriaEvent {
    private String accion;
    private String modulo;
    private LocalDateTime fecha;
    private Object data;


}