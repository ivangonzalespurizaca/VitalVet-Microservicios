package com.pacientes.api.services.impl;

import com.pacientes.api.entity.Mascota;
import com.pacientes.api.repository.MascotaRepository;
import com.pacientes.api.services.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class MascotaServiceImpl extends ICRUDImpl<Mascota, Long> implements MascotaService {

    @Autowired
    private MascotaRepository repo;

    @Override
    public JpaRepository<Mascota, Long> getRepository() {
        return repo;
    }
}
