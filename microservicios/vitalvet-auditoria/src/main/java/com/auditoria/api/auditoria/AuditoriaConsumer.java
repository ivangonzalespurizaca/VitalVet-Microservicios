package com.auditoria.api.auditoria;

import com.auditoria.api.documentos.AuditoriaDocument;
import com.auditoria.api.event.AuditoriaEvent;
import com.auditoria.api.repository.AuditoriaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaConsumer {

    private final ObjectMapper objectMapper;
    private final AuditoriaRepository auditoriaRepository;


    public AuditoriaConsumer(ObjectMapper objectMapper, AuditoriaRepository auditoriaRepository) {
        this.objectMapper = objectMapper;
        this.auditoriaRepository = auditoriaRepository;
    }

    @KafkaListener(topics = "test-limpio", groupId = "auditoria-group")
    public void escucharAuditoria(String mensajeJson) {
        if (!mensajeJson.trim().startsWith("{")) {
            System.out.println("[Kafka Consumer] Ignorando mensaje antiguo o formato incorrecto: " + mensajeJson);
            return;
        }

        try {
            AuditoriaEvent evento = objectMapper.readValue(mensajeJson, AuditoriaEvent.class);
            System.out.println("[Kafka Consumer] Evento recibido: " + evento.getAccion());

            AuditoriaDocument documento = new AuditoriaDocument();
            documento.setAccion(evento.getAccion());
            documento.setModulo(evento.getModulo());
            documento.setIdUsuario(evento.getIdUsuario());
            documento.setDescripcion(evento.getDescripcion());
            documento.setFecha(evento.getFecha());

            
            AuditoriaDocument guardado = (AuditoriaDocument) auditoriaRepository.save(documento);
            System.out.println("[MongoDB] Auditoría guardada con éxito. ID asignado: " + guardado.getId());

        } catch (JsonProcessingException e) {
            System.err.println("Error al mapear el JSON entrante a AuditoriaEvent");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error al persistir en MongoDB");
            e.printStackTrace();
        }
    }
}
