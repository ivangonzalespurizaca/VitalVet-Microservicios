package com.auditoria.api.auditoria;

import com.auditoria.api.documentos.AuditoriaDocument;

import com.auditoria.api.dtos.CitaClient;
import com.auditoria.api.dtos.DatosClientTransfer;
import com.auditoria.api.dtos.HorarioR;
import com.auditoria.api.event.AuditoriaEvent;
import com.auditoria.api.repository.AuditoriaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuditoriaConsumer {

    private final ObjectMapper objectMapper;
    private final AuditoriaRepository auditoriaRepository;


    public AuditoriaConsumer(ObjectMapper objectMapper, AuditoriaRepository auditoriaRepository) {
        this.objectMapper = objectMapper;
        this.auditoriaRepository = auditoriaRepository;
    }

    @KafkaListener(topics = "vitalvet-auditoria-events", groupId = "auditoria-group")
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
            documento.setFecha(evento.getFecha());

            switch (evento.getModulo()) {
                case "USUARIOS" -> {
                    DatosClientTransfer datos = objectMapper.convertValue(
                            evento.getData(),
                            DatosClientTransfer.class
                    );

                    documento.setIdUsuario(datos.getIdUsuario());
                    documento.setDescripcion(datos.getDescripcion());
                }

                case "CITAS" -> {
                      CitaClient data = objectMapper.convertValue(
                            evento.getData(),
                            CitaClient.class
                    );

                    documento.setIdUsuario(data.getIdveterinario());
                    documento.setDescripcion(
                            "Cita con el Veterinario ID #" + data.getIdveterinario() +
                                    " creada con estado " + data.getEstadoCita()
                    );
                }
                case "HORARIO"->{
                    HorarioR horario = objectMapper.convertValue(
                            evento.getData(),
                            HorarioR.class
                    );

                    documento.setIdUsuario(horario.getIdVeterinario());
                    documento.setDescripcion(
                            "Horario #" + horario.getIdHorario() +
                                    " generado correctamente"
                    );
                }

                default -> {
                    throw new IllegalArgumentException(
                            "Módulo no soportado: " + evento.getModulo()
                    );
                }
            }

            auditoriaRepository.save(documento);

        } catch (JsonProcessingException e) {
            System.err.println("Error al mapear el JSON entrante a AuditoriaEvent");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error al persistir en MongoDB");
            e.printStackTrace();
        }
    }
}
