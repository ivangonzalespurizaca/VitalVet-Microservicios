package com.vitalvet.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/paciente/mascota/contar/**").permitAll()
                        .requestMatchers("/api/paciente/mascota/public/**").permitAll()
                        .requestMatchers("/api/paciente/mascota/interno/**").permitAll()
                        .requestMatchers("/api/paciente/mascota/total-activos").permitAll()
                        .requestMatchers("/api/paciente/consulta/cita/interno/**").permitAll()
                        .requestMatchers("/api/paciente/especie/listar-activos").permitAll()
                        .requestMatchers("/api/paciente/raza/listar-activos").permitAll()
                        .requestMatchers("/api/paciente/gestion-vacunas/proximas/**").permitAll()
                        .requestMatchers("/api/paciente/mascota/cliente/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}