package com.usuarios.api.controller;

import com.usuarios.api.dto.LoginRequest;
import com.usuarios.api.dto.RegistroClienteRequest;
import com.usuarios.api.entity.Persona;
import com.usuarios.api.services.AuthService;
import com.usuarios.api.utils.ApiResponse;
import com.usuarios.api.utils.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/usuario/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest loginRequest) {

        String token = authService.login(loginRequest);
        AuthResponse authResponse = new AuthResponse(token);

        ApiResponse<AuthResponse> response = new ApiResponse<>(
                true,
                "¡Inicio de sesión exitoso!",
                authResponse
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/registrar/cliente")
    public ResponseEntity<ApiResponse<Long>> registrarCliente(@Valid @RequestBody RegistroClienteRequest request) {
        Persona clienteCreado = authService.registrarCliente(request);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "¡Usuario registrado exitosamente!",
                clienteCreado.getIdPersona()
        ));
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<ApiResponse<?>> solicitarRecuperacion(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es requerido.");
        }

        authService.procesarSolicitudRecuperacion(email.trim());

        ApiResponse<?> response = new ApiResponse<>(
                true,
                "¡Se ha enviado un enlace de recuperación a tu correo electrónico con éxito!",
                null
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<ApiResponse<?>> cambiarContrasenia(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String nuevaContrasenia = request.get("nuevaContrasenia");
        if (token == null || token.isEmpty() || nuevaContrasenia == null || nuevaContrasenia.isEmpty()) {
            throw new IllegalArgumentException("El token y la nueva contraseña son requeridos.");
        }
        try {
            authService.cambiarContraseniaOlvidada(token, nuevaContrasenia);
            return ResponseEntity.ok(new ApiResponse<>(true, "¡Tu contraseña ha sido restablecida con éxito!", null));
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
