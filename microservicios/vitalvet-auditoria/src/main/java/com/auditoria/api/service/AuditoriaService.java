package com.auditoria.api.service;

import com.auditoria.api.documentos.AuditoriaDocument;
import com.auditoria.api.event.AuditoriaEvent;
import com.auditoria.api.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditoriaService {
    @Autowired
    private AuditoriaRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;



    public List<AuditoriaDocument>listar(){
        return repository.findAll();
    }

    public List<AuditoriaDocument> filtros(String usuarioOrId, String modulo, String accion, LocalDate fechaRegistro) {
        Query query = new Query();
        List<Criteria> criterias = new ArrayList<>();

        // 1. Filtro: Buscar por Usuario o ID
        if (StringUtils.hasText(usuarioOrId)) {
            String cleanInput = usuarioOrId.trim();

            // Verificamos si ingresó un número (para buscar por idUsuario numérico)
            if (cleanInput.matches("\\d+")) {
                criterias.add(Criteria.where("idUsuario").is(Long.parseLong(cleanInput)));
            } else {
                // Si es texto, busca coincidencia parcial (ignore case) en un campo de texto (ej: "descripcion" o "usuario" si lo agregas)
                criterias.add(Criteria.where("descripcion").regex(cleanInput, "i"));
            }
        }

        // 2. Filtro: Tabla Afectada / Módulo (Evitamos "Todas")
        if (StringUtils.hasText(modulo) && !modulo.equalsIgnoreCase("Todas")) {
            criterias.add(Criteria.where("modulo").is(modulo.trim()));
        }

        // 3. Filtro: Acción Realizada (Evitamos "Todas")
        if (StringUtils.hasText(accion) && !accion.equalsIgnoreCase("Todas")) {
            criterias.add(Criteria.where("accion").is(accion.trim()));
        }

        // 4. Filtro: Fecha de Registro (Maneja todo el rango de ese día: 00:00:00 a 23:59:59)
        if (fechaRegistro != null) {
            LocalDateTime inicioDia = fechaRegistro.atStartOfDay();
            LocalDateTime finDia = fechaRegistro.atTime(LocalTime.MAX);

            criterias.add(Criteria.where("fecha").gte(inicioDia).lte(finDia));
        }

        if (!criterias.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[0])));
        }

        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "fecha"));

        return mongoTemplate.find(query, AuditoriaDocument.class);
    }





}
