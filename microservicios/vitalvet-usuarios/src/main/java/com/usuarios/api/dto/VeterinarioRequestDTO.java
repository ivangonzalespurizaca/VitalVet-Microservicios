package com.usuarios.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VeterinarioRequestDTO {
    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(min = 2, max = 60, message = "Los nombres deben tener entre 2 y 60 caracteres.")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(min = 2, max = 60, message = "Los apellidos deben tener entre 2 y 60 caracteres.")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio.")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe contener exactamente 8 dígitos numéricos.")
    private String dni;

    @Pattern(regexp = "^9\\d{8}$", message = "El celular debe empezar con 9 y contener exactamente 9 dígitos.")
    private String celular;

    @NotBlank(message = "El género es obligatorio.")
    @Pattern(regexp = "^(MASCULINO|FEMENINO)$", message = "El género debe ser MASCULINO o FEMENINO.")
    private String genero;

    @Email(message = "El formato del correo electrónico no es válido.")
    @Size(max = 80, message = "El correo no puede exceder los 80 caracteres.")
    private String email;

    @NotNull(message = "La especialidad es obligatoria.")
    private Long idEspecialidad;

    @NotBlank(message = "El número de colegiatura es obligatorio.")
    @Size(min = 4, max = 15, message = "El número de colegiatura debe tener entre 4 y 15 caracteres.")
    private String numColegiatura;
}
