package com.vitalvet.api.services.impl;

import com.vitalvet.api.client.UsuarioClient;
import com.vitalvet.api.dto.ClienteResponseDTO;
import com.vitalvet.api.dto.MascotaResponseDTO;
import com.vitalvet.api.entity.Mascota;
import com.vitalvet.api.http.response.MascotasResponse;
import com.vitalvet.api.mapper.MascotaMapper;
import com.vitalvet.api.repository.MascotaRepository;
import com.vitalvet.api.services.MascotaService;
import com.vitalvet.api.utils.ModeloNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MascotaServiceImpl extends ICRUDImpl<Mascota, Long> implements MascotaService {

    @Autowired
    private MascotaRepository repo;

    @Autowired
    private UsuarioClient usuarioClient;

    @Autowired
    private MascotaMapper mascotaMapper;

    @Override
    public JpaRepository<Mascota, Long> getRepository() {
        return repo;
    }

    @Override
    public int contarPorIdCliente(Long id) {
        return repo.countByIdClienteAndActivoTrue(id);
    }

    @Override
    public List<Mascota> listarMascotasPorCliente(Long idCliente) {
        return repo.findByIdClienteConRazaYEspecie(idCliente);
    }

    @Override
    @Transactional(readOnly = true)
    public MascotasResponse listarMascotasPorDniCliente(String dni) {

        ClienteResponseDTO cliente = usuarioClient.obtenerPropietarioPorDniInterno(dni);
        if (cliente == null) {
            throw new ModeloNotFoundException("No se encontró ningún cliente registrado con el DNI: " + dni);
        }

        List<Mascota> entidadesMascota = repo.findByIdClienteConRazaYEspecie(cliente.getIdPersona());
        List<MascotaResponseDTO> listaMascotasDTO = entidadesMascota.stream()
                .map(mascotaMapper::toResponseDTO)
                .toList();

        return MascotasResponse.builder()
                .idPersona(cliente.getIdPersona())
                .nombres(cliente.getNombres())
                .apellidos(cliente.getApellidos())
                .email(cliente.getEmail())
                .dni(cliente.getDni())
                .celular(cliente.getCelular())
                .totalMascotas(cliente.getTotalMascotas())
                .mascotas(listaMascotasDTO)
                .build();
    }

    @Override
    public boolean existePorId(Long idMascota) {
        return repo.existsById(idMascota);
    }

    @Override
    public Long contarActivos() {
        return repo.countByActivoTrue();
    }
}
