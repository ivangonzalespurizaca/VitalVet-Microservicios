package com.vitalvet.api.services.impl;

import com.vitalvet.api.dto.AlertaVacunaDTO;
import com.vitalvet.api.dto.CarnetVacunaDTO;
import com.vitalvet.api.dto.VacunaRegistroRequestDTO;
import com.vitalvet.api.dto.VacunaResponseDTO;
import com.vitalvet.api.entity.*;
import com.vitalvet.api.entity.enums.EstadoVacuna;
import com.vitalvet.api.repository.MascotaRepository;
import com.vitalvet.api.repository.VacunaAplicadaRepository;
import com.vitalvet.api.repository.VacunaRepository;
import com.vitalvet.api.services.VacunaAplicadaService;
import com.vitalvet.api.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class VacunaAplicadaServiceImpl extends ICRUDImpl<VacunaAplicada, Long> implements VacunaAplicadaService {

    @Autowired
    private VacunaAplicadaRepository repo;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private VacunaRepository vacunaRepository;

    @Override
    public JpaRepository<VacunaAplicada, Long> getRepository() {
        return repo;
    }

    @Transactional(readOnly = true)
    @Override
    public CarnetVacunaDTO obtenerCarnetCompletoPorMascota(Long idMascota, String estadoFiltro) {

        Mascota mascota = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new BusinessException("La mascota con ID " + idMascota + " no existe."));

        List<VacunaAplicada> historialCompleto = repo.findHistorialVacunacion(idMascota);
        List<VacunaResponseDTO> vacunasFiltradas = historialCompleto.stream()
                .filter(va -> {
                    if (estadoFiltro == null || estadoFiltro.isBlank() || "TODAS".equalsIgnoreCase(estadoFiltro)) {
                        return true;
                    }
                    return va.getEstado().name().equalsIgnoreCase(estadoFiltro.trim());
                })
                .map(va -> {
                    LocalDate fechaAMostrar = EstadoVacuna.PROGRAMADA.equals(va.getEstado())
                            ? va.getProximaDosis()
                            : va.getFechaAplicacion();

                    return VacunaResponseDTO.builder()
                            .idAplicacion(va.getIdAplicacion())
                            .nombreVacuna(va.getVacuna().getNombreVacuna())
                            .dosis(va.getNroDosis())
                            .fechaAplicacion(fechaAMostrar)
                            .proximaDosis(va.getProximaDosis())
                            .estado(va.getEstado().name())
                            .build();
                })
                .toList();
        return CarnetVacunaDTO.builder()
                .codigoMascota(mascota.getCodigoMascota())
                .nombreMascota(mascota.getNombreMascota())
                .sexo(mascota.getSexo())
                .fechaNacimiento(mascota.getFechaNacimiento())
                .pesoActual(mascota.getPesoActual())
                .fotoUrl(mascota.getFotoUrl())
                .nombreRaza(mascota.getRaza() != null ? mascota.getRaza().getNombreRaza() : "Sin Raza")
                .nombreEspecie(mascota.getRaza() != null && mascota.getRaza().getEspecie() != null
                        ? mascota.getRaza().getEspecie().getNombreEspecie() : "Sin Especie")
                .vacunas(vacunasFiltradas)
                .build();
    }

    @Override
    public List<AlertaVacunaDTO> buscarVacunasProximas(List<Long> idsMascotas) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(30); // Solo las que vencen en los próximos 30 días

        List<VacunaAplicada> vacunas = repo.findByMascota_IdMascotaInAndProximaDosisAfter(idsMascotas, hoy);

        return vacunas.stream()
                .filter(v -> v.getProximaDosis().isBefore(limite))
                .map(v -> AlertaVacunaDTO.builder()
                        .nombreVacuna(v.getVacuna().getNombreVacuna())
                        .nombreMascota(v.getMascota().getNombreMascota())
                        .fechaVencimiento(v.getProximaDosis())
                        .nivelAlerta("PRONTO")
                        .build())
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public VacunaAplicada registrarVacunaManual(VacunaRegistroRequestDTO dto, Long idMascota) {

        Mascota mascota = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new BusinessException("La mascota no existe."));

        VacunaAplicada nuevaAplicacion = new VacunaAplicada();
        nuevaAplicacion.setMascota(mascota);
        nuevaAplicacion.setNroDosis(dto.getDosisTipo());

        Vacuna vacuna = vacunaRepository.findById(dto.getIdVacuna())
                .orElseThrow(() -> new BusinessException("La vacuna seleccionada no existe en el catálogo."));
        nuevaAplicacion.setVacuna(vacuna);

        LocalDate fechaInput = dto.getFecha();
        LocalDate hoy = LocalDate.now();

        if (fechaInput.isAfter(hoy)) {
            nuevaAplicacion.setEstado(EstadoVacuna.PROGRAMADA);
            nuevaAplicacion.setProximaDosis(fechaInput);
            nuevaAplicacion.setFechaAplicacion(null);
            nuevaAplicacion.setConsulta(null);
        } else {
            nuevaAplicacion.setEstado(EstadoVacuna.APLICADA);
            nuevaAplicacion.setFechaAplicacion(fechaInput);
            nuevaAplicacion.setProximaDosis(null);
        }
        return repo.save(nuevaAplicacion);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void confirmarAplicacionVacuna(Long idAplicacion, Long idConsulta) {
        VacunaAplicada vacunaPending = repo.findById(idAplicacion)
                .orElseThrow(() -> new BusinessException("El registro de vacuna no existe."));

        if (!EstadoVacuna.PROGRAMADA.equals(vacunaPending.getEstado())) {
            throw new BusinessException("Esta vacuna ya ha sido aplicada o procesada anteriormente.");
        }

        vacunaPending.setEstado(EstadoVacuna.APLICADA);
        vacunaPending.setFechaAplicacion(LocalDate.now());

        if (idConsulta != null) {
            Consulta consulta = new Consulta();
            consulta.setIdConsulta(idConsulta);
            vacunaPending.setConsulta(consulta);
        }

        repo.save(vacunaPending);
    }

}
