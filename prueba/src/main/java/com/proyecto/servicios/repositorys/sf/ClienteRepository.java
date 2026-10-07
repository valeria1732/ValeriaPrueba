package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Integer>, JpaSpecificationExecutor<ClienteEntity> {

    Optional<ClienteEntity> findByCurp(String curp);

    Optional<ClienteEntity> findByRfc(String rfc);

    Optional<ClienteEntity> findByCorreo(String correo);

    boolean existsByCurp(String curp);

    boolean existsByRfc(String rfc);

    boolean existsByCorreo(String correo);

    List<ClienteEntity> findByActivo(Boolean activo);

    List<ClienteEntity> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT c FROM ClienteEntity c JOIN c.cuentas cu WHERE cu.numeroCuenta = :numeroCuenta")
    Optional<ClienteEntity> findByNumeroCuenta(@Param("numeroCuenta") String numeroCuenta);
}
