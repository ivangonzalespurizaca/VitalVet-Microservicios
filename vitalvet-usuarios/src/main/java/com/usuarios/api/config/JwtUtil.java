package com.usuarios.api.config;

import com.usuarios.api.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final long JWT_EXPIRATION_MS = 7200000;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date fechaExpiracion = new Date(ahora.getTime() + JWT_EXPIRATION_MS);

        var persona = usuario.getPersona();

        Map<String, Object> claims = new HashMap<>();
        claims.put("idUsuario", usuario.getIdUsuario());
        claims.put("email", usuario.getEmail());

        claims.put("idPersona", persona.getIdPersona());
        claims.put("nombres", persona.getNombres());
        claims.put("apellidos", persona.getApellidos());
        claims.put("dni", persona.getDni());
        claims.put("celular", persona.getCelular());
        claims.put("genero", persona.getGenero().name());
        claims.put("fotoUrl", persona.getFotoUrl());
        claims.put("rol", persona.getRol().name());
        if (usuario.getPersona().getRol() == com.usuarios.api.entity.enums.Rol.VETERINARIO
                && usuario.getPersona().getVeterinario() != null) {

            var vet = persona.getVeterinario();
            claims.put("numColegiatura", vet.getNumColegiatura());

            if (vet.getEspecialidad() != null) {
                claims.put("especialidad", vet.getEspecialidad().getNombreEspecialidad());
            }
        }
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claims(claims)
                .issuedAt(ahora)
                .expiration(fechaExpiracion)
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromJWT(String token) {
        return obtenerClaims(token).getSubject();
    }

    public Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
