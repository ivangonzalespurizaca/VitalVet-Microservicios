package com.agenda.api.repository;

import com.agenda.api.entity.Cita;
import com.agenda.api.entity.ComprobantePago;
import com.agenda.api.entity.enums.TipoComprobante;
import com.agenda.api.repository.custom.ComprobanteRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<ComprobantePago, Long>, ComprobanteRepositoryCustom {

    Long countByTipoDocumento(TipoComprobante tipoComprobante);

    Optional<ComprobantePago> findByCitaIdCita(Long idCita);
}
