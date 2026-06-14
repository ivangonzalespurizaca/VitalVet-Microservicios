package com.usuarios.api.services.impl;

import com.usuarios.api.entity.Usuario;
import com.usuarios.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService{

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getPersona().getRol().name());

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getContrasenia(),
                Collections.singletonList(authority)
        );
    }

}
