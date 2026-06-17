package com.pacientes.api.mapper;

import com.pacientes.api.dto.ConsultaDetalleDTO;
import com.pacientes.api.entity.Consulta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsultaMapper {

    ConsultaDetalleDTO toDto(Consulta entity);

    @Mapping(target = "idCita", ignore = true)
    @Mapping(target = "idVeterinario", ignore = true)
    Consulta toEntity(ConsultaDetalleDTO dto);
}
