package com.agenda.api.mapper;

import com.agenda.api.dto.HorarioDetalleDTO;
import com.agenda.api.dto.HorarioRequestDTO;
import com.agenda.api.entity.HorarioAtencion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HorarioMapper {

    HorarioDetalleDTO toDetalleDTO(HorarioAtencion entidad);

    List<HorarioDetalleDTO> toDetalleDTOList(List<HorarioAtencion> entidades);

    @Mapping(target = "idHorario", ignore = true)
    @Mapping(target = "activo", constant = "true")
    HorarioAtencion toEntity(HorarioRequestDTO dto);
}