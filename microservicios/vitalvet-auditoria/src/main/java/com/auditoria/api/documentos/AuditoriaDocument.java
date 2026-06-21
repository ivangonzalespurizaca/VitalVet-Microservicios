package com.auditoria.api.documentos;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "logs_auditoria") // Nombre de la colección en Mongo
public class AuditoriaDocument {

    @Id
    private String id;
    private String accion;
    private String modulo;
    private Long idUsuario;
    private String descripcion;
    private LocalDateTime fecha;
}