package com.agenda.api.services;

import com.agenda.api.dto.dashboard.AdminDashboardDTO;
import com.agenda.api.dto.dashboard.ClienteDashboardDTO;
import com.agenda.api.dto.dashboard.VeterinarioDashboardDTO;

public interface DashboardService {
    AdminDashboardDTO obtenerDatosAdmin();

    VeterinarioDashboardDTO obtenerDatosVeterinario(Long idVeterinario);

    ClienteDashboardDTO obtenerDatosCliente(Long idCliente);
}
