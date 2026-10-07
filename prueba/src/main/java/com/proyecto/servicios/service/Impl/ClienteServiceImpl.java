package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.ClienteEntity;
import com.proyecto.servicios.entity.sf.CuentaEntity;
import com.proyecto.servicios.entity.sf.DomicilioEntity;
import com.proyecto.servicios.entity.sf.UsuarioEntity;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.cliente.*;
import com.proyecto.servicios.model.cuenta.CuentaResponse;
import com.proyecto.servicios.model.usuario.UsuarioResponse;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.repositorys.sf.DomicilioRepository;
import com.proyecto.servicios.repositorys.sf.UsuarioRepository;
import com.proyecto.servicios.service.ClienteService;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final DomicilioRepository domicilioRepository;
    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public ClienteServiceImpl(
            ClienteRepository clienteRepository,
            DomicilioRepository domicilioRepository,
            CuentaRepository cuentaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.domicilioRepository = domicilioRepository;
        this.cuentaRepository = cuentaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ClienteResponse registrarCliente(ClienteRegistroRequest request) {
        log.info("Iniciando onboarding para cliente con CURP: {}, RFC: {}, Correo: {}",
                request.getCurp(), request.getRfc(), request.getCorreo());

        String curp = request.getCurp().trim().toUpperCase();
        String rfc = request.getRfc().trim().toUpperCase();
        String correo = request.getCorreo().trim().toLowerCase();

        // 1. Validaciones de negocio: Unicidad
        if (clienteRepository.existsByCurp(curp)) {
            throw new CurpDuplicadaException("Ya existe un cliente registrado con la CURP: " + curp);
        }
        if (clienteRepository.existsByRfc(rfc)) {
            throw new RfcDuplicadoException("Ya existe un cliente registrado con el RFC: " + rfc);
        }
        if (clienteRepository.existsByCorreo(correo) || usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoDuplicadoException("Ya existe un cliente o usuario registrado con el correo: " + correo);
        }

        // 2. Validación de mayoría de edad
        int edad = Period.between(request.getFechaNacimiento(), LocalDate.now()).getYears();
        if (edad < 18) {
            throw new ClienteBusinessException("El cliente debe ser mayor de edad para registrarse (edad actual: " + edad + " años)");
        }

        // 3. Crear y guardar ClienteEntity
        ClienteEntity cliente = ClienteEntity.builder()
                .nombre(request.getNombre().trim())
                .segundoNombre(StringUtils.trimToNull(request.getSegundoNombre()))
                .apellidoPaterno(request.getApellidoPaterno().trim())
                .apellidoMaterno(request.getApellidoMaterno().trim())
                .fechaNacimiento(request.getFechaNacimiento())
                .curp(curp)
                .rfc(rfc)
                .sexo(request.getSexo().trim().toUpperCase())
                .nacionalidad(StringUtils.defaultIfBlank(request.getNacionalidad(), "Mexicana").trim())
                .estadoCivil(request.getEstadoCivil().trim().toUpperCase())
                .correo(correo)
                .telefonoMovil(request.getTelefonoMovil().trim())
                .telefonoAlternativo(StringUtils.trimToNull(request.getTelefonoAlternativo()))
                .ocupacion(request.getOcupacion().trim())
                .empresa(request.getEmpresa().trim())
                .ingresoMensual(request.getIngresoMensual())
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        ClienteEntity clienteGuardado = clienteRepository.save(cliente);

        // 4. Crear y guardar DomicilioEntity
        DomicilioDTO domDTO = request.getDomicilio();
        DomicilioEntity domicilio = DomicilioEntity.builder()
                .cliente(clienteGuardado)
                .calle(domDTO.getCalle().trim())
                .numeroExterior(domDTO.getNumeroExterior().trim())
                .numeroInterior(StringUtils.trimToNull(domDTO.getNumeroInterior()))
                .colonia(domDTO.getColonia().trim())
                .municipio(domDTO.getMunicipio().trim())
                .estado(domDTO.getEstado().trim())
                .codigoPostal(domDTO.getCodigoPostal().trim())
                .pais(StringUtils.defaultIfBlank(domDTO.getPais(), "México").trim())
                .build();
        domicilioRepository.save(domicilio);
        clienteGuardado.setDomicilio(domicilio);

        // 5. Creación automática de Cuenta Bancaria
        BigDecimal saldoInicial = (request.getSaldoInicial() != null && request.getSaldoInicial().compareTo(BigDecimal.ZERO) >= 0)
                ? request.getSaldoInicial()
                : new BigDecimal("1000.00");

        CuentaEntity cuenta = generarCuentaBancaria(clienteGuardado, saldoInicial);
        CuentaEntity cuentaGuardada = cuentaRepository.save(cuenta);
        clienteGuardado.getCuentas().add(cuentaGuardada);

        // 6. Creación automática de Usuario de Acceso cifrado con BCrypt
        UsuarioEntity usuario = UsuarioEntity.builder()
                .cliente(clienteGuardado)
                .correo(correo)
                .password(passwordEncoder.encode(request.getPassword()))
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
        usuarioRepository.save(usuario);
        clienteGuardado.setUsuario(usuario);

        log.info("Onboarding completado exitosamente: Cliente ID={}, Cuenta={}, Usuario={}",
                clienteGuardado.getId(), cuentaGuardada.getNumeroCuenta(), usuario.getCorreo());

        return mapToResponse(clienteGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorId(Integer id) {
        log.info("Consultando cliente por ID: {}", id);
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + id));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorCurp(String curp) {
        log.info("Consultando cliente por CURP: {}", curp);
        ClienteEntity cliente = clienteRepository.findByCurp(curp.trim().toUpperCase())
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con la CURP: " + curp));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorRfc(String rfc) {
        log.info("Consultando cliente por RFC: {}", rfc);
        ClienteEntity cliente = clienteRepository.findByRfc(rfc.trim().toUpperCase())
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el RFC: " + rfc));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorCorreo(String correo) {
        log.info("Consultando cliente por Correo: {}", correo);
        ClienteEntity cliente = clienteRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el correo: " + correo));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorNumeroCuenta(String numeroCuenta) {
        log.info("Consultando cliente por número de cuenta: {}", numeroCuenta);
        ClienteEntity cliente = clienteRepository.findByNumeroCuenta(numeroCuenta.trim())
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente asociado a la cuenta: " + numeroCuenta));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> consultarClientes(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            String curp,
            String rfc,
            String correo,
            Boolean activo,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        Specification<ClienteEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(nombre)) {
                predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.trim().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(apellidoPaterno)) {
                predicates.add(cb.like(cb.lower(root.get("apellidoPaterno")), "%" + apellidoPaterno.trim().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(apellidoMaterno)) {
                predicates.add(cb.like(cb.lower(root.get("apellidoMaterno")), "%" + apellidoMaterno.trim().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(curp)) {
                predicates.add(cb.equal(cb.upper(root.get("curp")), curp.trim().toUpperCase()));
            }
            if (StringUtils.isNotBlank(rfc)) {
                predicates.add(cb.equal(cb.upper(root.get("rfc")), rfc.trim().toUpperCase()));
            }
            if (StringUtils.isNotBlank(correo)) {
                predicates.add(cb.like(cb.lower(root.get("correo")), "%" + correo.trim().toLowerCase() + "%"));
            }
            if (activo != null) {
                predicates.add(cb.equal(root.get("activo"), activo));
            }
            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaInicio.atStartOfDay()));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaFin.atTime(23, 59, 59)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return clienteRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClienteResponse actualizarCliente(Integer id, ClienteActualizaRequest request) {
        log.info("Actualización completa de cliente ID: {}", id);
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + id));

        cliente.setNombre(request.getNombre().trim());
        cliente.setSegundoNombre(StringUtils.trimToNull(request.getSegundoNombre()));
        cliente.setApellidoPaterno(request.getApellidoPaterno().trim());
        cliente.setApellidoMaterno(request.getApellidoMaterno().trim());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setSexo(request.getSexo().trim().toUpperCase());
        cliente.setNacionalidad(request.getNacionalidad().trim());
        cliente.setEstadoCivil(request.getEstadoCivil().trim().toUpperCase());
        cliente.setTelefonoMovil(request.getTelefonoMovil().trim());
        cliente.setTelefonoAlternativo(StringUtils.trimToNull(request.getTelefonoAlternativo()));
        cliente.setOcupacion(request.getOcupacion().trim());
        cliente.setEmpresa(request.getEmpresa().trim());
        cliente.setIngresoMensual(request.getIngresoMensual());

        if (request.getDomicilio() != null && cliente.getDomicilio() != null) {
            DomicilioDTO d = request.getDomicilio();
            DomicilioEntity dom = cliente.getDomicilio();
            dom.setCalle(d.getCalle().trim());
            dom.setNumeroExterior(d.getNumeroExterior().trim());
            dom.setNumeroInterior(StringUtils.trimToNull(d.getNumeroInterior()));
            dom.setColonia(d.getColonia().trim());
            dom.setMunicipio(d.getMunicipio().trim());
            dom.setEstado(d.getEstado().trim());
            dom.setCodigoPostal(d.getCodigoPostal().trim());
            dom.setPais(StringUtils.defaultIfBlank(d.getPais(), "México").trim());
            domicilioRepository.save(dom);
        }

        ClienteEntity actualizado = clienteRepository.save(cliente);
        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public ClienteResponse actualizarParcial(Integer id, ClientePatchRequest request) {
        log.info("Actualización parcial de cliente ID: {}", id);
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + id));

        if (request.getNombre() != null) cliente.setNombre(request.getNombre().trim());
        if (request.getSegundoNombre() != null) cliente.setSegundoNombre(StringUtils.trimToNull(request.getSegundoNombre()));
        if (request.getApellidoPaterno() != null) cliente.setApellidoPaterno(request.getApellidoPaterno().trim());
        if (request.getApellidoMaterno() != null) cliente.setApellidoMaterno(request.getApellidoMaterno().trim());
        if (request.getFechaNacimiento() != null) cliente.setFechaNacimiento(request.getFechaNacimiento());
        if (request.getSexo() != null) cliente.setSexo(request.getSexo().trim().toUpperCase());
        if (request.getNacionalidad() != null) cliente.setNacionalidad(request.getNacionalidad().trim());
        if (request.getEstadoCivil() != null) cliente.setEstadoCivil(request.getEstadoCivil().trim().toUpperCase());
        if (request.getTelefonoMovil() != null) cliente.setTelefonoMovil(request.getTelefonoMovil().trim());
        if (request.getTelefonoAlternativo() != null) cliente.setTelefonoAlternativo(StringUtils.trimToNull(request.getTelefonoAlternativo()));
        if (request.getOcupacion() != null) cliente.setOcupacion(request.getOcupacion().trim());
        if (request.getEmpresa() != null) cliente.setEmpresa(request.getEmpresa().trim());
        if (request.getIngresoMensual() != null) cliente.setIngresoMensual(request.getIngresoMensual());

        if (request.getDomicilio() != null && cliente.getDomicilio() != null) {
            DomicilioDTO d = request.getDomicilio();
            DomicilioEntity dom = cliente.getDomicilio();
            if (d.getCalle() != null) dom.setCalle(d.getCalle().trim());
            if (d.getNumeroExterior() != null) dom.setNumeroExterior(d.getNumeroExterior().trim());
            if (d.getNumeroInterior() != null) dom.setNumeroInterior(StringUtils.trimToNull(d.getNumeroInterior()));
            if (d.getColonia() != null) dom.setColonia(d.getColonia().trim());
            if (d.getMunicipio() != null) dom.setMunicipio(d.getMunicipio().trim());
            if (d.getEstado() != null) dom.setEstado(d.getEstado().trim());
            if (d.getCodigoPostal() != null) dom.setCodigoPostal(d.getCodigoPostal().trim());
            if (d.getPais() != null) dom.setPais(d.getPais().trim());
            domicilioRepository.save(dom);
        }

        ClienteEntity actualizado = clienteRepository.save(cliente);
        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public void bajaLogica(Integer id) {
        log.info("Ejecutando baja lógica integral para cliente ID: {}", id);
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + id));

        // Desactivar cliente
        cliente.setActivo(false);

        // Desactivar sus cuentas bancarias asociadas
        if (cliente.getCuentas() != null) {
            for (CuentaEntity c : cliente.getCuentas()) {
                c.setEstatus("INACTIVA");
                cuentaRepository.save(c);
            }
        }

        // Desactivar usuario de acceso asociado
        if (cliente.getUsuario() != null) {
            cliente.getUsuario().setActivo(false);
            usuarioRepository.save(cliente.getUsuario());
        }

        clienteRepository.save(cliente);
        log.info("Baja lógica completada exitosamente para cliente ID: {}", id);
    }

    private CuentaEntity generarCuentaBancaria(ClienteEntity cliente, BigDecimal saldoInicial) {
        String numeroCuenta;
        do {
            numeroCuenta = String.format("%010d", secureRandom.nextInt(1_000_000_000));
        } while (cuentaRepository.existsByNumeroCuenta(numeroCuenta));

        String clabe = "012180" + numeroCuenta + "01";

        return CuentaEntity.builder()
                .cliente(cliente)
                .numeroCuenta(numeroCuenta)
                .clabe(clabe)
                .saldo(saldoInicial)
                .estatus("ACTIVA")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    private ClienteResponse mapToResponse(ClienteEntity cliente) {
        StringBuilder sb = new StringBuilder(cliente.getNombre());
        if (StringUtils.isNotBlank(cliente.getSegundoNombre())) {
            sb.append(" ").append(cliente.getSegundoNombre());
        }
        sb.append(" ").append(cliente.getApellidoPaterno());
        sb.append(" ").append(cliente.getApellidoMaterno());

        DomicilioDTO domDTO = null;
        if (cliente.getDomicilio() != null) {
            DomicilioEntity d = cliente.getDomicilio();
            domDTO = DomicilioDTO.builder()
                    .calle(d.getCalle())
                    .numeroExterior(d.getNumeroExterior())
                    .numeroInterior(d.getNumeroInterior())
                    .colonia(d.getColonia())
                    .municipio(d.getMunicipio())
                    .estado(d.getEstado())
                    .codigoPostal(d.getCodigoPostal())
                    .pais(d.getPais())
                    .build();
        }

        List<CuentaResponse> cuentasResponse = new ArrayList<>();
        if (cliente.getCuentas() != null) {
            cuentasResponse = cliente.getCuentas().stream()
                    .map(c -> CuentaResponse.builder()
                            .id(c.getId())
                            .clienteId(cliente.getId())
                            .numeroCuenta(c.getNumeroCuenta())
                            .clabe(c.getClabe())
                            .saldo(c.getSaldo())
                            .estatus(c.getEstatus())
                            .fechaCreacion(c.getFechaCreacion())
                            .fechaActualizacion(c.getFechaActualizacion())
                            .build())
                    .collect(Collectors.toList());
        }

        UsuarioResponse usuarioResponse = null;
        if (cliente.getUsuario() != null) {
            UsuarioEntity u = cliente.getUsuario();
            usuarioResponse = UsuarioResponse.builder()
                    .id(u.getId())
                    .clienteId(cliente.getId())
                    .correo(u.getCorreo())
                    .activo(u.getActivo())
                    .fechaCreacion(u.getFechaCreacion())
                    .fechaActualizacion(u.getFechaActualizacion())
                    .build();
        }

        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .segundoNombre(cliente.getSegundoNombre())
                .apellidoPaterno(cliente.getApellidoPaterno())
                .apellidoMaterno(cliente.getApellidoMaterno())
                .nombreCompleto(sb.toString())
                .fechaNacimiento(cliente.getFechaNacimiento())
                .curp(cliente.getCurp())
                .rfc(cliente.getRfc())
                .sexo(cliente.getSexo())
                .nacionalidad(cliente.getNacionalidad())
                .estadoCivil(cliente.getEstadoCivil())
                .correo(cliente.getCorreo())
                .telefonoMovil(cliente.getTelefonoMovil())
                .telefonoAlternativo(cliente.getTelefonoAlternativo())
                .domicilio(domDTO)
                .ocupacion(cliente.getOcupacion())
                .empresa(cliente.getEmpresa())
                .ingresoMensual(cliente.getIngresoMensual())
                .activo(cliente.getActivo())
                .fechaCreacion(cliente.getFechaCreacion())
                .fechaActualizacion(cliente.getFechaActualizacion())
                .cuentas(cuentasResponse)
                .usuario(usuarioResponse)
                .build();
    }
}
