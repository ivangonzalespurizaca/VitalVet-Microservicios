package com.agenda.api.mapper;

import com.agenda.api.dto.CitaRequestDTO;
import com.agenda.api.dto.CitaResponseDTO;
import com.agenda.api.entity.Cita;
import com.agenda.api.http.response.CitaPanelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CitaMapper {

    @Mapping(target = "idCita", ignore = true)
    @Mapping(target = "codigoCita", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Cita toEntity(CitaRequestDTO dto);

    CitaResponseDTO toResponseDTO(Cita entity);
    CitaPanelResponse toPanelResponseDTO(Cita entity);

    List<CitaResponseDTO> toResponseDTOList(List<Cita> entities);
}