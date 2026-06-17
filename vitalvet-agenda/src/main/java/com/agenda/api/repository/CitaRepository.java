package com.agenda.api.repository;

import com.agenda.api.entity.Cita;
import com.agenda.api.entity.HorarioAtencion;
import com.agenda.api.entity.enums.DiaSemana;
import com.agenda.api.entity.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    @Query(value = "SELECT COUNT(*) > 0 FROM agenda.cita c " +
            "WHERE c.id_veterinario = :idVeterinario " +
            "AND c.estado = 'PENDIENTE' " +
            "AND c.fecha >= CURRENT_DATE " +
            "AND CAST(to_char(c.fecha, 'ID') AS INTEGER) = :nroDia",
            nativeQuery = true)
    boolean existsCitasPendientesFuturas(
            @Param("idVeterinario") Long idVeterinario,
            @Param("nroDia") int nroDia
    );

    @Query("SELECT c FROM Cita c WHERE (:estado IS NULL OR c.estado = :estado) " +
            "ORDER BY c.fecha DESC, c.hora ASC")
    List<Cita> listarCitasConFiltroEstado(@Param("estado") EstadoCita estado);

    @Query(value = "SELECT codigo_cita FROM agenda.cita WHERE fecha = :fecha ORDER BY codigo_cita DESC LIMIT 1", nativeQuery = true)
    Optional<String> findTopCodigoCitaByFechaOrderByCodigoCitaDesc(@Param("fecha") LocalDate fecha);

    @Query("SELECT c.hora FROM Cita c " +
            "WHERE c.idVeterinario = :idVeterinario " +
            "AND c.fecha = :fecha " +
            "AND c.estado != EstadoCita.CANCELADA")
    List<LocalTime> findHorasOcupadasNoCanceladas(
            @Param("idVeterinario") Long idVeterinario,
            @Param("fecha") LocalDate fecha
    );

    boolean existsByIdVeterinarioAndFechaAndHoraAndEstadoNot(Long idVeterinario, LocalDate fecha, LocalTime hora, EstadoCita estadoExcluir);

    Long countByIdMascotaAndEstado(Long idMascota, EstadoCita estado);

    Long countByFecha(LocalDate fecha);
}