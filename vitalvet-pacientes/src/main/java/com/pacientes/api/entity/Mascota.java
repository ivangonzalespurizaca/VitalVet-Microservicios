package com.pacientes.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pacientes.api.entity.enums.SexoMascota;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mascota", schema = "pacientes", indexes = {
        @Index(name = "idx_mascota_cliente", columnList = "id_cliente"),
        @Index(name = "idx_mascota_codigo", columnList = "codigo_mascota")
})
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mascota")
    private Long idMascota;

    @Column(name = "codigo_mascota", nullable = false, length = 20, unique = true)
    private String codigoMascota;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "nombre_mascota", nullable = false)
    private String nombreMascota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SexoMascota sexo;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "peso_actual", precision = 5, scale = 2)
    private BigDecimal pesoActual = BigDecimal.ZERO;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Column(nullable = false)
    private Boolean activo = true;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_raza", nullable = false,
            foreignKey = @ForeignKey(name = "fk_mascota_raza"))
    private Raza raza;

    @JsonIgnoreProperties("mascota")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    private List<VacunaAplicada> vacunasAplicadas;

}
