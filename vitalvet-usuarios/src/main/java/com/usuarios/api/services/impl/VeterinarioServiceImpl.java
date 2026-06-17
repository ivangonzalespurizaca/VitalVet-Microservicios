package com.usuarios.api.services.impl;

import com.usuarios.api.dto.VeterinarioHeaderDTO;
import com.usuarios.api.dto.VeterinarioRequestDTO;
import com.usuarios.api.entity.Especialidad;
import com.usuarios.api.entity.Persona;
import com.usuarios.api.entity.Usuario;
import com.usuarios.api.entity.Veterinario;
import com.usuarios.api.entity.enums.Genero;
import com.usuarios.api.entity.enums.Rol;
import com.usuarios.api.mapper.VeterinarioMapper;
import com.usuarios.api.repository.EspecialidadRepository;
import com.usuarios.api.repository.PersonaRepository;
import com.usuarios.api.repository.UsuarioRepository;
import com.usuarios.api.repository.VeterinarioRepository;
import com.usuarios.api.services.VeterinarioService;
import com.usuarios.api.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeterinarioServiceImpl extends ICRUDImpl<Veterinario, Long> implements VeterinarioService{

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private VeterinarioMapper vetMapper;


    @Override
    public JpaRepository<Veterinario, Long> getRepository() {
        return veterinarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Veterinario> listarVeterinarios(String criterio) {
        return veterinarioRepository.buscarPorCriterio(criterio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Veterinario> listarVeterinariosActivos() {
        return veterinarioRepository.listarActivos();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Veterinario registrarVeterinarioCompleto(VeterinarioRequestDTO dto) {

        if (personaRepository.existsByDni(dto.getDni())) {
            throw new BusinessException("El número de DNI '" + dto.getDni() + "' ya se encuentra registrado en el sistema.");
        }

        if (usuarioRepository.findByEmail(dto.getEmail().trim()).isPresent()) {
            throw new BusinessException("El correo electrónico ya se encuentra registrado.");
        }

        if (veterinarioRepository.existsByNumColegiatura(dto.getNumColegiatura())){
            throw new BusinessException("El número de colegiatura " + dto.getNumColegiatura() + " ya existe");
        }

        Persona persona = vetMapper.toPersona(dto);
        persona.setRol(Rol.VETERINARIO);
        persona.setActivo(true);
        persona.setCodigoPersona("VET-2026-" + dto.getDni());
        Persona personaGuardada = personaRepository.save(persona);

        Usuario usuario = vetMapper.toUsuario(dto);
        usuario.setPersona(personaGuardada);
        usuario.setContrasenia(passwordEncoder.encode(dto.getDni()));
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Especialidad esp = especialidadRepository.findById(dto.getIdEspecialidad())
                .orElseThrow(() -> new BusinessException("La especialidad no existe"));

        Veterinario veterinario = vetMapper.toVeterinario(dto);
        veterinario.setPersona(personaGuardada);
        veterinario.setEspecialidad(esp);
        personaGuardada.setUsuario(usuarioGuardado);

        return veterinarioRepository.save(veterinario);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Veterinario actualizarVeterinarioCompleto(Long idVeterinario, VeterinarioRequestDTO dto) {
        Veterinario veterinarioExistente = veterinarioRepository.findById(idVeterinario)
                .orElseThrow(() -> new BusinessException("Veterinario no encontrado"));

        Persona personaExistente = veterinarioExistente.getPersona();

        if (!personaExistente.getDni().trim().equals(dto.getDni().trim())) {
            if (personaRepository.existsByDni(dto.getDni().trim())) {
                throw new BusinessException("No se puede actualizar: El número de DNI '" + dto.getDni().trim() + "' ya pertenece a otro usuario.");
            }
        }

        if (!veterinarioExistente.getNumColegiatura().trim().equals(dto.getNumColegiatura().trim())) {
            if (veterinarioRepository.existsByNumColegiatura(dto.getNumColegiatura().trim())) {
                throw new BusinessException("No se puede actualizar: El número de colegiatura '" + dto.getNumColegiatura().trim() + "' ya se encuentra asignado a otro veterinario.");
            }
        }

        vetMapper.updatePersonaFromDto(dto, personaExistente);
        vetMapper.updateVeterinarioFromDto(dto, veterinarioExistente);

        if (!veterinarioExistente.getEspecialidad().getIdEspecialidad().equals(dto.getIdEspecialidad())) {
            Especialidad nuevaEsp = especialidadRepository.findById(dto.getIdEspecialidad())
                    .orElseThrow(() -> new BusinessException("La especialidad especificada no existe"));
            veterinarioExistente.setEspecialidad(nuevaEsp);
        }

        return veterinarioRepository.save(veterinarioExistente);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void desactivarVeterinario(Long idVeterinario) {
        Veterinario veterinario = veterinarioRepository.findById(idVeterinario)
                .orElseThrow(() -> new BusinessException("El veterinario con el ID " + idVeterinario + " no existe."));

        Persona persona = veterinario.getPersona();

        persona.setActivo(false);

        personaRepository.save(persona);
        veterinarioRepository.save(veterinario);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activarVeterinario(Long idVeterinario) {
        Veterinario veterinario = veterinarioRepository.findById(idVeterinario)
                .orElseThrow(() -> new BusinessException("El veterinario con el ID " + idVeterinario + " no existe."));

        Persona persona = veterinario.getPersona();

        persona.setActivo(true);

        personaRepository.save(persona);
        veterinarioRepository.save(veterinario);
    }
}