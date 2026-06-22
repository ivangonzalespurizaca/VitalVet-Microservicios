package com.agenda.api.services.impl;

import com.agenda.api.client.PacienteClient;
import com.agenda.api.client.UsuarioClient;
import com.agenda.api.dto.*;
import com.agenda.api.dto.dashboard.*;
import com.agenda.api.entity.Cita;
import com.agenda.api.entity.ComprobantePago;
import com.agenda.api.entity.enums.TipoEstadoCita;
import com.agenda.api.repository.CitaRepository;
import com.agenda.api.repository.ComprobanteRepository;
import com.agenda.api.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteClient pacienteClient;

    @Autowired
    private UsuarioClient usuarioClient;

    @Override
    public AdminDashboardDTO obtenerDatosAdmin() {
        List<ComprobantePago> pagosEntidad = comprobanteRepository.obtenerUltimosPagos(PageRequest.of(0, 5));

        List<ComprobanteRecienteDTO> pagosDTO = pagosEntidad.stream().map(p -> {
            // Usamos el cliente que YA tienes para obtener el detalle completo
            ClienteResponseDTO cliente = usuarioClient.obtenerDetalleCliente(p.getIdCliente());

            String nombreCompleto = "Cliente Desconocido";
            if (cliente != null) {
                nombreCompleto = cliente.getNombres() + " " + cliente.getApellidos();
            }

            return ComprobanteRecienteDTO.builder()
                    .nombreCliente(nombreCompleto)
                    .codigoComprobante(p.getCodigoComprobante())
                    .montoTotal(p.getMontoTotal())
                    .metodoPago(p.getMetodoPago().name())
                    .build();
        }).collect(Collectors.toList());

        return AdminDashboardDTO.builder()
                .ingresosTotalesHoy(comprobanteRepository.sumarIngresosHoy() != null ?
                        comprobanteRepository.sumarIngresosHoy() : BigDecimal.ZERO)
                .cantidadCitasHoy(citaRepository.countByFecha(LocalDate.now()))
                .cantidadPacientesRegistrados(pacienteClient.obtenerTotalPacientes())
                .ultimosPagos(pagosDTO)
                .citasPorEstado(this.convertirAEstadoMap(citaRepository.contarCitasPorEstadoRaw()))
                .build();
    }

    @Override
    public VeterinarioDashboardDTO obtenerDatosVeterinario(Long idVeterinario) {
        LocalDate hoy = LocalDate.now();

        // 1. Contadores (rápidos, contra tu BD local)
        Long enEspera = citaRepository.countByFechaAndIdVeterinarioAndEstado(hoy, idVeterinario, TipoEstadoCita.PAGADA);
        Long atendidos = citaRepository.countByFechaAndIdVeterinarioAndEstado(hoy, idVeterinario, TipoEstadoCita.COMPLETADA);

        // 2. Traer todas las citas de hoy
        List<Cita> todasLasCitas = citaRepository.findByFechaAndIdVeterinarioOrderByHoraAsc(hoy, idVeterinario);

        // 3. Enriquecer próximas citas (PAGADAS) con nombre real de Mascota
        List<CitaProximaDTO> proximas = todasLasCitas.stream()
                .filter(c -> c.getEstado() == TipoEstadoCita.PAGADA)
                .map(c -> {
                    // LLAMADA AL CLIENTE: Aquí obtenemos el nombre real
                    MascotaResponseDTO mascota = pacienteClient.obtenerDetalleMascota(c.getIdMascota());
                    String nombre = (mascota != null) ? mascota.getNombreMascota() : "Mascota " + c.getIdMascota();

                    return CitaProximaDTO.builder()
                            .nombreMascota(nombre)
                            .hora(c.getHora().toString())
                            .motivo(c.getMotivo())
                            .build();
                })
                .toList();

        // 4. Enriquecer atenciones recientes (COMPLETADAS)
        List<CitaProximaDTO> atenciones = todasLasCitas.stream()
                .filter(c -> c.getEstado() == TipoEstadoCita.COMPLETADA)
                .map(c -> {
                    MascotaResponseDTO mascota = pacienteClient.obtenerDetalleMascota(c.getIdMascota());
                    String nombre = (mascota != null) ? mascota.getNombreMascota() : "Mascota " + c.getIdMascota();

                    return CitaProximaDTO.builder()
                            .nombreMascota(nombre)
                            .hora(c.getHora().toString())
                            .motivo(c.getMotivo())
                            .build();
                })
                .toList();

        return VeterinarioDashboardDTO.builder()
                .pacientesEnEspera(enEspera)
                .pacientesAtendidosHoy(atendidos)
                .proximasCitas(proximas)
                .atencionesRecientes(atenciones)
                .siguientePaciente(!proximas.isEmpty() ? proximas.get(0).getNombreMascota() : "Ninguno")
                .build();
    }

    @Override
    public ClienteDashboardDTO obtenerDatosCliente(Long idCliente) {
        // 1. Obtener datos básicos
        List<MascotaResponseDTO> mascotas = pacienteClient.obtenerMascotasPorCliente(idCliente);
        List<Long> idsMascotas = mascotas.stream()
                .map(MascotaResponseDTO::getIdMascota)
                .collect(Collectors.toList());

        // 2. Obtener citas y vacunas
        List<Cita> citas = citaRepository.findProximasCitasPorMascotas(idsMascotas);
        List<AlertaVacunaDTO> vacunas = pacienteClient.obtenerVacunasProximasPorIds(idsMascotas);

        // 3. Mapa para nombres de mascota
        Map<Long, String> mapaNombres = mascotas.stream()
                .collect(Collectors.toMap(MascotaResponseDTO::getIdMascota, MascotaResponseDTO::getNombreMascota));

        // 4. Mapeo final (Simplificado al extremo)
        return ClienteDashboardDTO.builder()
                .nombreCliente(usuarioClient.obtenerDetalleCliente(idCliente).getNombres())
                .cantidadMascotas((long) mascotas.size())
                .proximasCitas(citas.stream().map(c -> {
                    // Mapeo seguro: todo convertido a String manualmente
                    return CitaProximaDTO.builder()
                            .nombreMascota(mapaNombres.getOrDefault(c.getIdMascota(), "Mascota"))
                            .fecha(c.getFecha() != null ? c.getFecha().toString() : "Sin fecha")
                            .hora(c.getHora() != null ? c.getHora().toString() : "Sin hora")
                            .motivo(c.getMotivo() != null ? c.getMotivo() : "Sin motivo")
                            .estado(c.getEstado() != null ? c.getEstado().name() : "PENDIENTE")
                            .build();
                }).toList())
                .vacunasProximas(vacunas != null ? vacunas : List.of())
                .build();
    }



    // Método privado para la conversión
    private Map<String, Long> convertirAEstadoMap(List<Object[]> resultados) {
        return resultados.stream().collect(java.util.stream.Collectors.toMap(
                obj -> {
                    Object estado = obj[0];
                    return (estado instanceof Enum) ? ((Enum<?>) estado).name() : estado.toString();
                },
                obj -> (Long) obj[1]
        ));
    }
}
