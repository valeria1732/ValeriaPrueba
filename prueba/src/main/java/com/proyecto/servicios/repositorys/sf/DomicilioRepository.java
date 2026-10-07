package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.DomicilioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomicilioRepository extends JpaRepository<DomicilioEntity, Integer> {

    Optional<DomicilioEntity> findByClienteId(Integer clienteId);
}
