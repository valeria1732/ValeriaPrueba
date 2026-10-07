package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.config.SecurityConfig;
import com.proyecto.servicios.entity.sf.ClienteEntity;
import com.proyecto.servicios.entity.sf.UsuarioEntity;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.usuario.LoginRequest;
import com.proyecto.servicios.model.usuario.LoginResponse;
import com.proyecto.servicios.model.usuario.UsuarioCreacionRequest;
import com.proyecto.servicios.model.usuario.UsuarioResponse;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.service.UsuarioService;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#\\-])[A-Za-z\\d@$!%*?&._#\\-]{8,}$");

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityConfig securityConfig;

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              ClienteRepository clienteRepository,
                              PasswordEncoder passwordEncoder,
                              SecurityConfig securityConfig) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityConfig = securityConfig;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Intento de inicio de sesión para el correo: {}", request.getCorreo());
        String correoLimpio = request.getCorreo().trim().toLowerCase();

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correoLimpio)
                .orElseThrow(() -> new UsuarioNotFoundException("No se encontró el usuario con correo: " + request.getCorreo()));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            log.warn("Intento de inicio de sesión para usuario inactivo: {}", correoLimpio);
            throw new UsuarioInactivoException("El usuario se encuentra inactivo. Acceso denegado");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            log.warn("Contraseña incorrecta para el usuario: {}", correoLimpio);
            throw new CredencialesInvalidasException("Credenciales incorrectas");
        }

        Integer clienteId = usuario.getCliente() != null ? usuario.getCliente().getId() : null;
        String token = securityConfig.generarToken(usuario.getCorreo(), clienteId, usuario.getId());
        log.info("Inicio de sesión exitoso para usuario: {}", correoLimpio);

        return LoginResponse.builder()
                .token(token)
                .tipoToken("Bearer")
                .correo(usuario.getCorreo())
                .clienteId(clienteId)
                .expiraEnMs(86400000L)
                .build();
    }

    @Override
    @Transactional
    public UsuarioResponse agregarUsuario(UsuarioCreacionRequest request) {
        log.info("Creando usuario de acceso manual para cliente ID: {}", request.getClienteId());

        ClienteEntity cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + request.getClienteId()));

        if (usuarioRepository.findByClienteId(cliente.getId()).isPresent()) {
            throw new ClienteBusinessException("Cada cliente podrá tener únicamente un usuario asociado");
        }

        String correoLimpio = request.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correoLimpio)) {
            throw new CorreoDuplicadoException("No pueden existir dos usuarios con el mismo correo electrónico: " + correoLimpio);
        }

        if (request.getPassword() == null || !PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new ContrasenaInvalidaException("La contraseña debe tener mínimo 8 caracteres, al menos una letra mayúscula, una minúscula, un número y un carácter especial");
        }

        UsuarioEntity nuevoUsuario = UsuarioEntity.builder()
                .cliente(cliente)
                .correo(correoLimpio)
                .password(passwordEncoder.encode(request.getPassword()))
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        UsuarioEntity guardado = usuarioRepository.save(nuevoUsuario);
        log.info("Usuario creado exitosamente con ID: {}", guardado.getId());

        return mapToResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> consultarUsuariosConFiltro(String correo, Boolean activo) {
        log.info("Consultando usuarios con filtro correo: {}, activo: {}", correo, activo);

        Specification<UsuarioEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(correo)) {
                predicates.add(cb.like(cb.lower(root.get("correo")), "%" + correo.trim().toLowerCase() + "%"));
            }
            if (activo != null) {
                predicates.add(cb.equal(root.get("activo"), activo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return usuarioRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UsuarioResponse mapToResponse(UsuarioEntity usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .clienteId(usuario.getCliente() != null ? usuario.getCliente().getId() : null)
                .correo(usuario.getCorreo())
                .activo(usuario.getActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaActualizacion(usuario.getFechaActualizacion())
                .build();
    }
}
