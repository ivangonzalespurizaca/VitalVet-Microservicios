package com.usuarios.api.mapper;

import com.usuarios.api.dto.PerfilRequestDTO;
import com.usuarios.api.dto.PerfilResponseDTO;
import com.usuarios.api.dto.PersonaRequestDTO;
import com.usuarios.api.entity.Persona;
import com.usuarios.api.entity.Usuario;
import com.usuarios.api.http.response.ClienteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PersonaMapper {
    @Mapping(target = "idPersona", ignore = true)
    @Mapping(target = "codigoPersona", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "dni", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fotoUrl", ignore = true)
    void updatePersonaFromDto(PerfilRequestDTO dto, @MappingTarget Persona persona);

    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "numColegiatura", ignore = true)
    PerfilResponseDTO toResponseDTO(Persona persona, Usuario usuario);

    @Mapping(target = "totalMascotas", ignore = true)
    @Mapping(target = "email", source = "persona", qualifiedByName = "mapearEmailSeguro")
    ClienteResponse toClienteResponseDTO(Persona persona);

    @Named("mapearEmailSeguro")
    default String mapearEmailSeguro(Persona persona) {
        if (persona != null && persona.getUsuario() != null) {
            return persona.getUsuario().getEmail();
        }
        return "Registrado desde mostrador";
    }

    @Mapping(target = "idPersona", ignore = true)
    @Mapping(target = "codigoPersona", ignore = true)
    @Mapping(target = "fotoUrl", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "veterinario", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Persona toEntity(PersonaRequestDTO dto);

    @Mapping(target = "idPersona", ignore = true)
    @Mapping(target = "codigoPersona", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "veterinario", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    void updatePersonaFromRequestDTO(PersonaRequestDTO dto, @MappingTarget Persona persona);
}
