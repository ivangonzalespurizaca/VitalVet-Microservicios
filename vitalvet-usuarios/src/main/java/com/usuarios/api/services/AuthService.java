package com.usuarios.api.services;

import com.usuarios.api.dto.LoginRequest;
import com.usuarios.api.dto.RegistroClienteRequest;
import com.usuarios.api.entity.Persona;

public interface AuthService {
    String login(LoginRequest loginRequest);
    Persona registrarCliente(RegistroClienteRequest registroRequest);
    void procesarSolicitudRecuperacion(String email);
    void cambiarContraseniaOlvidada(String token, String nuevaContrasenia);
}
