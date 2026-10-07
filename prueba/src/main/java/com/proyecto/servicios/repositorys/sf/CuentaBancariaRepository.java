package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.CuentaBancariaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaBancariaRepository extends JpaRepository<CuentaBancariaEntity, Integer> {

    Optional<CuentaBancariaEntity> findByClienteId(Integer clienteId);

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByClabe(String clabe);
}
