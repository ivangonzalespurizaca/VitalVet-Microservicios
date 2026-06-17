package com.pacientes.api.services;

import com.pacientes.api.entity.Consulta;

import java.util.Optional;

public interface ConsultaService extends ICRUD<Consulta, Long>{
    Optional<Consulta> buscarPorIdCita (Long idCita);
}