package com.pacientes.api.mapper;

import com.pacientes.api.dto.MascotaResponseDTO;
import com.pacientes.api.dto.MascotaRequestDTO;
import com.pacientes.api.entity.Mascota;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MascotaMapper {
    @Mapping(target = "idMascota", ignore = true)
    @Mapping(target = "raza", ignore = true)
    @Mapping(target = "codigoMascota", ignore = true)
    @Mapping(target = "vacunasAplicadas", ignore = true)
    Mascota toEntity(MascotaRequestDTO dto);

    @Mapping(target = "nombreRaza", source = "raza.nombreRaza")
    MascotaResponseDTO toResponseDTO(Mascota mascota);

    @AfterMapping
    default void generarCamposAutomaticos(MascotaRequestDTO dto, @MappingTarget Mascota mascota) {
        mascota.setCodigoMascota("MAS-" + dto.getIdCliente() + "-" + System.currentTimeMillis());
        mascota.setActivo(true);
    }
}
