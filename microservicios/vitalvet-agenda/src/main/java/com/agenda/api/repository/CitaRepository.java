package com.agenda.api.repository;

import com.agenda.api.entity.Cita;
import com.agenda.api.entity.enums.TipoEstadoCita;
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
            "AND c.estado = 'PAGADA' " +
            "AND c.fecha >= CURRENT_DATE " +
            "AND CAST(to_char(c.fecha, 'ID') AS INTEGER) = :nroDia",
            nativeQuery = true)
    boolean existsCitasPagadasFuturas(
            @Param("idVeterinario") Long idVeterinario,
            @Param("nroDia") int nroDia
    );

    List<Cita> findByEstadoOrderByFechaDescHoraAsc(TipoEstadoCita estado);

    List<Cita> findAllByOrderByFechaDescHoraAsc();

    @Query(value = "SELECT codigo_cita FROM agenda.cita WHERE fecha = :fecha ORDER BY codigo_cita DESC LIMIT 1", nativeQuery = true)
    Optional<String> findTopCodigoCitaByFechaOrderByCodigoCitaDesc(@Param("fecha") LocalDate fecha);


    @Query("SELECT c.hora FROM Cita c " +
            "WHERE c.idVeterinario = :idVeterinario " +
            "AND c.fecha = :fecha " +
            "AND c.estado != TipoEstadoCita.CANCELADA")
    List<LocalTime> findHorasOcupadasNoCanceladas(
            @Param("idVeterinario") Long idVeterinario,
            @Param("fecha") LocalDate fecha
    );

    boolean existsByIdVeterinarioAndFechaAndHoraAndEstadoNot(Long idVeterinario, LocalDate fecha, LocalTime hora, TipoEstadoCita estadoExcluir);

    Long countByIdMascotaAndEstado(Long idMascota, TipoEstadoCita estado);

    Long countByFecha(LocalDate fecha);

    @Query("SELECT c.estado, COUNT(c) FROM Cita c GROUP BY c.estado")
    List<Object[]> contarCitasPorEstadoRaw();

    // 1. Contar pacientes en espera (Citas para hoy con estado 'ESPERA')
    Long countByFechaAndIdVeterinarioAndEstado(LocalDate fecha, Long idVeterinario, TipoEstadoCita estado);

    // 3. Proximas citas (Lista para la agenda rápida)
    List<Cita> findByFechaAndIdVeterinarioOrderByHoraAsc(LocalDate fecha, Long idVeterinario);


        // Cambia esto a solo buscar por lista de mascotas
        @Query("SELECT c FROM Cita c WHERE c.idMascota IN :idsMascotas AND c.fecha >= CURRENT_DATE() ORDER BY c.fecha ASC, c.hora ASC")
        List<Cita> findProximasCitasPorMascotas(@Param("idsMascotas") List<Long> idsMascotas);




}