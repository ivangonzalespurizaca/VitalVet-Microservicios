package com.agenda.api.repository;

import com.agenda.api.entity.HorarioAtencion;
import com.agenda.api.entity.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Long>{
    List<HorarioAtencion> findByIdVeterinarioAndActivoTrueOrderByDiaSemanaAsc(Long idVeterinario);
    boolean existsByIdVeterinarioAndDiaSemana(Long idVeterinario, DiaSemana diaSemana);
    Optional<HorarioAtencion> findByIdVeterinarioAndDiaSemanaAndActivoTrue(Long idVeterinario, DiaSemana diaSemana);
}
