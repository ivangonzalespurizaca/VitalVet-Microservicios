package com.pacientes.api.services.impl;

import com.pacientes.api.entity.Consulta;
import com.pacientes.api.repository.ConsultaRepository;
import com.pacientes.api.services.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConsultaServiceImpl extends ICRUDImpl<Consulta, Long> implements ConsultaService{

    @Autowired
    private ConsultaRepository consultaRepository;

    @Override
    public JpaRepository<Consulta, Long> getRepository() {
        return consultaRepository;
    }

    @Override
    public Optional<Consulta> buscarPorIdCita(Long idCita) {
        return consultaRepository.findByIdCita(idCita);
    }
}