package com.vitalvet.api.repository;

import com.vitalvet.api.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    int countByIdClienteAndActivoTrue(Long idCliente);
    @Query("SELECT m FROM Mascota m " +
            "JOIN FETCH m.raza r " +
            "JOIN FETCH r.especie e " +
            "WHERE m.idCliente = :idCliente AND m.activo = true")
    List<Mascota> findByIdClienteConRazaYEspecie(@Param("idCliente") Long idCliente);

    @Modifying
    @Transactional // Garantiza que la mutación se ejecute de forma segura en la BD
    @Query("UPDATE Mascota m SET m.pesoActual = :peso WHERE m.idMascota = :idMascota")
    void actualizarPesoMascota(@Param("idMascota") Long idMascota, @Param("peso") BigDecimal peso);

    Long countByActivoTrue();

}
