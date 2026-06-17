package com.usuarios.api.mapper;

import com.usuarios.api.dto.VeterinarioHeaderDTO;
import com.usuarios.api.dto.VeterinarioRequestDTO;
import com.usuarios.api.dto.VeterinarioResponseDTO;
import com.usuarios.api.entity.Persona;
import com.usuarios.api.entity.Usuario;
import com.usuarios.api.entity.Veterinario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VeterinarioMapper {

    @Mapping(target = "idVeterinario", source = "veterinario.idVeterinario")
    @Mapping(target = "nombres", source = "veterinario.persona.nombres")
    @Mapping(target = "apellidos", source = "veterinario.persona.apellidos")
    @Mapping(target = "dni", source = "veterinario.persona.dni")
    @Mapping(target = "fotoUrl", source = "veterinario.persona.fotoUrl")
    @Mapping(target = "activo", source = "veterinario.persona.activo")
    @Mapping(target = "nroColegiatura", source = "veterinario.numColegiatura")
    @Mapping(target = "especialidad", source = "veterinario.especialidad.nombreEspecialidad")
    @Mapping(target = "email", source = "veterinario.persona.usuario.email")
    VeterinarioResponseDTO toResponseDTO(Veterinario veterinario);

    @Mapping(target = "idPersona", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "codigoPersona", ignore = true)
    Persona toPersona(VeterinarioRequestDTO dto);

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "contrasenia", ignore = true)
    Usuario toUsuario(VeterinarioRequestDTO dto);

    @Mapping(target = "idVeterinario", ignore = true)
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "especialidad", ignore = true)
    Veterinario toVeterinario(VeterinarioRequestDTO dto);

    @Mapping(target = "idPersona", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "codigoPersona", ignore = true)
    void updatePersonaFromDto(VeterinarioRequestDTO dto, @MappingTarget Persona persona);

    @Mapping(target = "idVeterinario", ignore = true)
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "especialidad", ignore = true)
    void updateVeterinarioFromDto(VeterinarioRequestDTO dto, @MappingTarget Veterinario veterinario);


    @Mapping(target = "dni", source = "persona.dni")
    @Mapping(target = "nombreCompleto", expression = "java(veterinario.getPersona().getNombres() + \" \" + veterinario.getPersona().getApellidos())")
    @Mapping(target = "especialidad", expression = "java(veterinario.getEspecialidad() != null ? veterinario.getEspecialidad().getNombreEspecialidad() : \"GENERAL\")")
    VeterinarioHeaderDTO toHeaderDTO(Veterinario veterinario);
}
