package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.CuentaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, Integer> {

    Optional<CuentaEntity> findByNumeroCuenta(String numeroCuenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CuentaEntity c WHERE c.numeroCuenta = :numeroCuenta")
    Optional<CuentaEntity> findByNumeroCuentaWithLock(@Param("numeroCuenta") String numeroCuenta);

    List<CuentaEntity> findByClienteId(Integer clienteId);

    org.springframework.data.domain.Page<CuentaEntity> findByClienteId(Integer clienteId, org.springframework.data.domain.Pageable pageable);

    List<CuentaEntity> findByEstatus(String estatus);

    org.springframework.data.domain.Page<CuentaEntity> findByEstatus(String estatus, org.springframework.data.domain.Pageable pageable);

    List<CuentaEntity> findByEstatusAndClienteActivoTrue(String estatus);

    org.springframework.data.domain.Page<CuentaEntity> findByClienteIdAndEstatus(Integer clienteId, String estatus, org.springframework.data.domain.Pageable pageable);

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByClabe(String clabe);
}
