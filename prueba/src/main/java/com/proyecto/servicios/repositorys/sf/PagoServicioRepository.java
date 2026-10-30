package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.PagoServicioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoServicioRepository extends JpaRepository<PagoServicioEntity, Integer> {

    List<PagoServicioEntity> findByNumeroCuenta(String numeroCuenta);

    Page<PagoServicioEntity> findByNumeroCuenta(String numeroCuenta, Pageable pageable);

    List<PagoServicioEntity> findByClienteId(Integer clienteId);

    Optional<PagoServicioEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<PagoServicioEntity> findByFolioAutorizacion(String folioAutorizacion);

    boolean existsByNumeroCuentaAndIdServicioAndIdProductoAndTelefonoReferenciaAndPrecioAndEstatusTransaccionInAndFechaCreacionAfter(
            String numeroCuenta,
            Integer idServicio,
            Integer idProducto,
            String telefonoReferencia,
            BigDecimal precio,
            Collection<String> estatus,
            LocalDateTime fechaDesde
    );
}
