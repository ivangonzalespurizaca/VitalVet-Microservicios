package com.agenda.api.services.impl;

import com.agenda.api.client.UsuarioClient;
import com.agenda.api.dto.HorarioDetalleDTO;
import com.agenda.api.dto.VeterinarioHeaderDTO;
import com.agenda.api.entity.HorarioAtencion;
import com.agenda.api.entity.enums.DiaSemana;
import com.agenda.api.http.response.HorarioResponse;
import com.agenda.api.mapper.HorarioMapper;
import com.agenda.api.repository.CitaRepository;
import com.agenda.api.repository.HorarioAtencionRepository;
import com.agenda.api.services.HorarioAtencionService;
import com.agenda.api.utils.BusinessException;
import com.agenda.api.utils.ModeloNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HorarioAtencionServiceImpl extends ICRUDImpl<HorarioAtencion, Long> implements HorarioAtencionService {
    @Autowired
    private HorarioAtencionRepository repo;

    @Autowired
    private UsuarioClient veterinarioClient;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorarioMapper horarioMapper;

    @Override
    public JpaRepository<HorarioAtencion, Long> getRepository() {
        return repo;
    }

    @Override
    public HorarioResponse listarPorVet(Long id) {
        VeterinarioHeaderDTO headerDTO;
        try {
            headerDTO = veterinarioClient.obtenerCabecera(id);
        } catch (feign.FeignException.NotFound e) {
            throw new ModeloNotFoundException("El veterinario con ID: " + id + " no existe en el sistema.");
        }

        if (headerDTO == null) {
            throw new BusinessException("El veterinario con el código proporcionado no existe.");
        }

        List<HorarioAtencion> horariosEntidad = repo.findByIdVeterinarioAndActivoTrueOrderByDiaSemanaAsc(id);
        List<HorarioDetalleDTO> listaDetallesDTO = horarioMapper.toDetalleDTOList(horariosEntidad);

        return HorarioResponse.builder()
                .dni(headerDTO.getDni())
                .nombreCompleto(headerDTO.getNombreCompleto())
                .especialidad(headerDTO.getEspecialidad())
                .horarios(listaDetallesDTO).build();
    }

    @Override
    public boolean existePorfechaYVeterinario(Long id, DiaSemana dia){
        return repo.existsByIdVeterinarioAndDiaSemana(id, dia);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void eliminarHorarioFisico(Long idHorario) {
        HorarioAtencion horario = repo.findById(idHorario)
                .orElseThrow(() -> new BusinessException("El horario de atención solicitado no existe."));
        int nroDiaPostgres = horario.getDiaSemana().ordinal() + 1;

        boolean tieneCitasPendientes = citaRepository.existsCitasPendientesFuturas(
                horario.getIdVeterinario(),
                nroDiaPostgres
        );

        if (tieneCitasPendientes) {
            throw new BusinessException("No se puede eliminar: El veterinario cuenta con citas médicas PENDIENTES agendadas para el día " + horario.getDiaSemana() + ".");
        }

        repo.delete(horario);
    }


}
