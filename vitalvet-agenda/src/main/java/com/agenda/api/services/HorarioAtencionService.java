package com.agenda.api.services;

import com.agenda.api.dto.HorarioRequestDTO;
import com.agenda.api.entity.HorarioAtencion;
import com.agenda.api.entity.enums.DiaSemana;
import com.agenda.api.http.response.HorarioResponse;

public interface HorarioAtencionService extends ICRUD<HorarioAtencion, Long>{
    public HorarioResponse listarPorVet(Long id);
    boolean existePorfechaYVeterinario(Long id, DiaSemana dia);
    void eliminarHorarioFisico(Long idHorario);
}
