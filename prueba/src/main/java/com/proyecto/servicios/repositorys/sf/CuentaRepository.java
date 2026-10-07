package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.CuentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, Integer> {

    Optional<CuentaEntity> findByNumeroCuenta(String numeroCuenta);

    List<CuentaEntity> findByClienteId(Integer clienteId);

    List<CuentaEntity> findByEstatus(String estatus);

    List<CuentaEntity> findByEstatusAndClienteActivoTrue(String estatus);

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByClabe(String clabe);
}
