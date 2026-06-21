package com.usuarios.api.controller;

import com.usuarios.api.client.PacienteClient;
import com.usuarios.api.dto.*;
import com.usuarios.api.entity.Persona;
import com.usuarios.api.http.response.ClienteResponse;
import com.usuarios.api.kafka.event.AuditoriaEvent;
import com.usuarios.api.kafka.producer.AuditoriaProducer;
import com.usuarios.api.mapper.PersonaMapper;
import com.usuarios.api.services.AuthService;
import com.usuarios.api.services.PersonaService;
import com.usuarios.api.utils.ApiResponse;
import com.usuarios.api.utils.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/usuario/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private PersonaMapper personaMapper;

    @Autowired
    private PacienteClient pacienteClient;

    @Autowired
    private AuditoriaProducer producer;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        String token = authService.login(loginRequestDTO);

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡Inicio de sesión exitoso!", new AuthResponse(token)
        ));
    }

    @PostMapping("/registrar/web")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ApiResponse<?>> registrarDesdeWeb(@Valid @RequestBody RegistroWebDTO request) {

        Persona clienteCreado = authService.registrarCliente(request.getDatosCliente());

        ClienteResponse clienteResponseDTO = personaMapper.toClienteResponseDTO(clienteCreado);
        clienteResponseDTO.setEmail(request.getDatosCliente().getEmail());

        MascotaRequestDTO mascotaDTO = request.getDatosMascota();
        mascotaDTO.setIdCliente(clienteCreado.getIdPersona());

        try {
            pacienteClient.registrarMascotaPublicoInterno(mascotaDTO);


            AuditoriaEvent eventoAuditoria = new AuditoriaEvent(
                    "REGISTRO",
                    "MASCOTAS",
                    clienteResponseDTO.getIdPersona(),
                    "Se registró exitosamente la mascota: " + mascotaDTO.getNombreMascota(),
                    LocalDateTime.now()
            );
            producer.enviar(eventoAuditoria);

            clienteResponseDTO.setTotalMascotas(1);
        } catch (Exception e) {
            throw new RuntimeException("Error en el registro distribuido del paciente clínico: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true, "¡Cuenta de cliente y mascota registrados con éxito desde la web!", clienteResponseDTO
        ));
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<ApiResponse<Void>> solicitarRecuperacion(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es requerido.");
        }

        authService.procesarSolicitudRecuperacion(email.trim());

        return ResponseEntity.ok(new ApiResponse<>(
                true, "¡Se ha enviado un enlace de recuperación a tu correo electrónico con éxito!", null
        ));
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<ApiResponse<Void>> cambiarContrasenia(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String nuevaContrasenia = request.get("nuevaContrasenia");

        if (token == null || token.trim().isEmpty() || nuevaContrasenia == null || nuevaContrasenia.trim().isEmpty()) {
            throw new IllegalArgumentException("El token y la nueva contraseña son requeridos.");
        }

        authService.cambiarContraseniaOlvidada(token, nuevaContrasenia);
        return ResponseEntity.ok(new ApiResponse<>(true, "¡Tu contraseña ha sido restablecida con éxito!", null));
    }
}