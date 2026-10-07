package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer>, JpaSpecificationExecutor<UsuarioEntity> {

    Optional<UsuarioEntity> findByCorreo(String correo);

    Optional<UsuarioEntity> findByClienteId(Integer clienteId);

    boolean existsByCorreo(String correo);

    List<UsuarioEntity> findByActivo(Boolean activo);
}
