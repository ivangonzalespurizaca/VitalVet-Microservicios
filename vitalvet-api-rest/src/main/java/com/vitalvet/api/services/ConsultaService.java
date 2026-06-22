package com.vitalvet.api.services;

import com.vitalvet.api.dto.ConsultaRequestDTO;
import com.vitalvet.api.dto.ConsultaResumenDTO;
import com.vitalvet.api.entity.Consulta;

import java.util.List;
import java.util.Optional;

public interface ConsultaService extends ICRUD<Consulta, Long>{
    Optional<Consulta> buscarPorIdCita (Long idCita);

    Consulta registrarConsultaClinica(ConsultaRequestDTO dto, Long idMascota);

}