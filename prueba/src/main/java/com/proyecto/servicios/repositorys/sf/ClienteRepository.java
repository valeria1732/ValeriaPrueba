package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Integer>, JpaSpecificationExecutor<ClienteEntity> {

    Optional<ClienteEntity> findByCurp(String curp);

    Optional<ClienteEntity> findByRfc(String rfc);

    boolean existsByCurp(String curp);

    boolean existsByRfc(String rfc);

    List<ClienteEntity> findByActivo(Boolean activo);
}
