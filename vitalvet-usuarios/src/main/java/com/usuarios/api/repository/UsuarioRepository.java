package com.usuarios.api.repository;

import com.usuarios.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByTokenRecuperacion(String token);
    @Query("SELECT u FROM Usuario u JOIN FETCH u.persona WHERE u.email = :email")
    Optional<Usuario> findByEmailConPersona(@Param("email") String email);
}
