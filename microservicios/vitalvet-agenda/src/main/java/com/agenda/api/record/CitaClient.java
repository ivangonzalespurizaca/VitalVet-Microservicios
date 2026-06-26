package com.agenda.api.record;

import com.agenda.api.entity.enums.TipoEstadoCita;

public record CitaClient(
        Long idUsuario,Long idveterinario,TipoEstadoCita estadoCita
) {

}
