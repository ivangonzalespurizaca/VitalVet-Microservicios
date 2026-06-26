package com.agenda.api.kafka.producer;

import com.agenda.api.kafka.event.AuditoriaEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditoriaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void enviar(AuditoriaEvent event) {
        try {

            String jsonEvent = objectMapper.writeValueAsString(event);

            kafkaTemplate.send("vitalvet-auditoria-events", jsonEvent)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            System.out.println("[Kafka Producer] EVENTO ENVIADO - Offset: "
                                    + result.getRecordMetadata().offset() + " | Payload: " + jsonEvent);
                        } else {
                            System.err.println("[Kafka Producer] Error al enviar a Kafka");
                            ex.printStackTrace();
                        }
                    });

        } catch (JsonProcessingException e) {
            System.err.println("Error al serializar el evento AuditoriaEvent a JSON");
            e.printStackTrace();
        }
    }
}