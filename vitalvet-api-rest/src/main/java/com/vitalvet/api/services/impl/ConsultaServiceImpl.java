package com.vitalvet.api.services.impl;

import com.vitalvet.api.client.AgendaClient;
import com.vitalvet.api.dto.ConsultaRequestDTO;
import com.vitalvet.api.dto.ConsultaResumenDTO;
import com.vitalvet.api.dto.VacunaAplicadaDTO;
import com.vitalvet.api.entity.Consulta;
import com.vitalvet.api.entity.Mascota;
import com.vitalvet.api.entity.Vacuna;
import com.vitalvet.api.entity.VacunaAplicada;
import com.vitalvet.api.entity.enums.EstadoVacuna;
import com.vitalvet.api.repository.ConsultaRepository;
import com.vitalvet.api.repository.MascotaRepository;
import com.vitalvet.api.repository.VacunaAplicadaRepository;
import com.vitalvet.api.repository.VacunaRepository;
import com.vitalvet.api.services.ConsultaService;
import com.vitalvet.api.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConsultaServiceImpl extends ICRUDImpl<Consulta, Long> implements ConsultaService{

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private VacunaAplicadaRepository vacunaAplicadaRepository;

    @Autowired
    private VacunaRepository vacunaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private AgendaClient agendaClient;

    @Override
    public JpaRepository<Consulta, Long> getRepository() {
        return consultaRepository;
    }

    @Override
    public Optional<Consulta> buscarPorIdCita(Long idCita) {
        return consultaRepository.findByIdCita(idCita);
    }

    @Override
    public Consulta registrarConsultaClinica(ConsultaRequestDTO dto, Long idMascota) {

        if (consultaRepository.existsByIdCita(dto.getIdCita())) {
            throw new BusinessException("Operación Denegada: Esta cita ya cuenta con un historial médico registrado.");
        }

        Consulta consulta = new Consulta();
        consulta.setIdCita(dto.getIdCita());
        consulta.setIdVeterinario(dto.getIdVeterinario());
        consulta.setPesoActual(dto.getPesoActual());
        consulta.setTemperatura(dto.getTemperatura());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setRecomendaciones(dto.getRecomendaciones());

        Consulta consultaGuardada = consultaRepository.save(consulta);

        if (dto.getVacunas() != null && !dto.getVacunas().isEmpty()) {
            LocalDate hoy = LocalDate.now();


            for (VacunaAplicadaDTO vDto : dto.getVacunas()) {
                VacunaAplicada vacunaAplicada = new VacunaAplicada();

                LocalDate fechaInput = (vDto.getProximaDosis() != null) ? vDto.getProximaDosis() : hoy;

                if (fechaInput.isAfter(hoy)) {
                    vacunaAplicada.setEstado(EstadoVacuna.PROGRAMADA);
                    vacunaAplicada.setProximaDosis(fechaInput);
                    vacunaAplicada.setFechaAplicacion(null);
                    vacunaAplicada.setConsulta(null);
                } else {
                    vacunaAplicada.setEstado(EstadoVacuna.APLICADA);
                    vacunaAplicada.setFechaAplicacion(fechaInput);
                    vacunaAplicada.setProximaDosis(null);
                    vacunaAplicada.setConsulta(consultaGuardada);
                }

                vacunaAplicada.setNroDosis(vDto.getNroDosis() != null ? vDto.getNroDosis() : "1ra Dosis");

                Mascota mascota = mascotaRepository.findById(idMascota)
                        .orElseThrow(() -> new BusinessException("Mascota no encontrada"));
                vacunaAplicada.setMascota(mascota);

                Vacuna vacuna = vacunaRepository.findById(vDto.getIdVacuna())
                        .orElseThrow(() -> new BusinessException("Vacuna no encontrada en catálogo"));
                vacunaAplicada.setVacuna(vacuna);

                vacunaAplicadaRepository.save(vacunaAplicada);
            }
        }

        if (dto.getPesoActual() != null) {
            mascotaRepository.actualizarPesoMascota(idMascota, dto.getPesoActual());
        }

        try {
            agendaClient.completarCitaInterno(dto.getIdCita());
        } catch (Exception e) {
            System.err.println("Error al actualizar el estado de la cita vía Feign: " + e.getMessage());
        }

        return consultaGuardada;
    }
}