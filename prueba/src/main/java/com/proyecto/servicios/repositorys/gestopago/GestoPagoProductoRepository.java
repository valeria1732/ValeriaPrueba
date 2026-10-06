package com.proyecto.servicios.repositorys.gestopago;

import com.proyecto.servicios.entity.gestopago.GestoPagoProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GestoPagoProductoRepository extends JpaRepository<GestoPagoProductoEntity, Integer> {

    Optional<GestoPagoProductoEntity> findByCodigo(String codigo);
}
