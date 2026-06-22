package com.vitalvet.api.repository;

import com.vitalvet.api.entity.VacunaAplicada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacunaAplicadaRepository extends JpaRepository<VacunaAplicada, Long> {
    @Query("SELECT va FROM VacunaAplicada va JOIN FETCH va.vacuna v WHERE va.mascota.idMascota = :idMascota ORDER BY va.fechaAplicacion DESC, va.proximaDosis ASC")
    List<VacunaAplicada> findHistorialVacunacion(@Param("idMascota") Long idMascota);

    List<VacunaAplicada> findByMascota_IdMascotaInAndProximaDosisAfter(List<Long> idsMascotas, LocalDate fecha);
}
