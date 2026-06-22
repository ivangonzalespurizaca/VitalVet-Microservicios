package com.agenda.api.repository;

import com.agenda.api.dto.ComprobanteRecienteDTO;
import com.agenda.api.entity.ComprobantePago;
import com.agenda.api.entity.enums.TipoComprobante;
import com.agenda.api.repository.custom.ComprobanteRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<ComprobantePago, Long>, ComprobanteRepositoryCustom {

    @Query(value = "SELECT SUM(monto_total) FROM agenda.comprobante_pago WHERE DATE(fecha_pago) = CURRENT_DATE", nativeQuery = true)
    BigDecimal sumarIngresosHoy();

    @Query("SELECT p FROM ComprobantePago p ORDER BY p.fechaPago DESC")
    List<ComprobantePago> obtenerUltimosPagos(Pageable pageable);

    Long countByTipoDocumento(TipoComprobante tipoComprobante);

    Optional<ComprobantePago> findByCitaIdCita(Long idCita);
}
