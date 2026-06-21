package com.auditoria.api.repository;

import com.auditoria.api.documentos.AuditoriaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends MongoRepository<AuditoriaDocument,String> {
}
