package com.usuarios.api.services.impl;

import com.usuarios.api.config.JwtUtil;
import com.usuarios.api.dto.LoginRequestDTO;
import com.usuarios.api.dto.PersonaRequestDTO;
import com.usuarios.api.dto.RegistroClienteRequestDTO;
import com.usuarios.api.entity.Persona;
import com.usuarios.api.entity.Usuario;
import com.usuarios.api.entity.enums.Rol;
import com.usuarios.api.mapper.PersonaMapper;
import com.usuarios.api.repository.PersonaRepository;
import com.usuarios.api.repository.UsuarioRepository;
import com.usuarios.api.services.AuthService;
import com.usuarios.api.utils.BadCredentialsException;
import com.usuarios.api.utils.BusinessException;
import com.usuarios.api.utils.ModeloNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PersonaMapper personaMapper;

    @Override
    public String login(LoginRequestDTO loginRequestDTO) {

        Usuario usuario = usuarioRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas (Email no encontrado)"));

        if (Boolean.FALSE.equals(usuario.getPersona().getActivo())) {
            throw new org.springframework.security.authentication.DisabledException(
                    "Su cuenta se encuentra desactivada. No puede ingresar al sistema."
            );
        }

        if (!passwordEncoder.matches(loginRequestDTO.getContrasenia(), usuario.getContrasenia())) {
            throw new BadCredentialsException("Credenciales incorrectas (Contraseña inválida)");
        }

        return jwtUtil.generarToken(usuario);
    }

    @Override
    @Transactional
    public Persona registrarCliente(PersonaRequestDTO dto) {

        if (personaRepository.existsByDni(dto.getDni())) {
            throw new BusinessException("El cliente con el DNI " + dto.getDni() + " ya está registrado.");
        }

        Persona persona = personaMapper.toEntity(dto);
        persona.setRol(Rol.CLIENTE);
        persona.setActivo(true);
        persona.setCodigoPersona("CLI-" + dto.getDni());

        Persona personaGuardada = personaRepository.save(persona);

        if (dto instanceof RegistroClienteRequestDTO webDto) {

            if (usuarioRepository.findByEmail(webDto.getEmail().trim()).isPresent()) {
                throw new BusinessException("El correo electrónico ya se encuentra registrado.");
            }

            Usuario usuario = new Usuario();
            usuario.setEmail(webDto.getEmail().trim());
            usuario.setContrasenia(passwordEncoder.encode(webDto.getContrasenia()));
            usuario.setFechaCreacion(java.time.LocalDateTime.now());
            usuario.setPersona(personaGuardada);
            usuarioRepository.save(usuario);
        }

        return personaGuardada;
    }

    @Override
    public void procesarSolicitudRecuperacion(String email) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ModeloNotFoundException("No existe ningún usuario registrado con este correo electrónico."));

        String tokenTemporal = UUID.randomUUID().toString();

        usuario.setTokenRecuperacion(tokenTemporal);
        usuario.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        emailService.enviarCorreoRecuperacion(usuario.getEmail(), tokenTemporal);
    }

    @Override
    public void cambiarContraseniaOlvidada(String token, String nuevaContrasenia) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacion(token)
                .orElseThrow(() -> new ModeloNotFoundException("El enlace de recuperación no es válido o ya fue utilizado."));

        if (usuario.getFechaExpiracionToken().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("El enlace de recuperación ha expirado. Por favor, solicita uno nuevo.");
        }

        usuario.setContrasenia(passwordEncoder.encode(nuevaContrasenia));
        usuario.setTokenRecuperacion(null);
        usuario.setFechaExpiracionToken(null);

        usuarioRepository.save(usuario);
    }

}
