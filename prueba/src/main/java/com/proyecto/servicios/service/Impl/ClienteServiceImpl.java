package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.*;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.PageResponse;
import com.proyecto.servicios.model.cliente.*;
import com.proyecto.servicios.model.cuenta.CuentaResponse;
import com.proyecto.servicios.model.usuario.UsuarioResponse;
import com.proyecto.servicios.repositorys.sf.*;
import com.proyecto.servicios.service.ClienteService;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;
import java.util.stream.*;


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
    public PageResponse<ClienteResponse> consultarClientesPaginados(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            String curp,
            String rfc,
            String correo,
            Boolean activo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            int page,
            int size) {

        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : 10;
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Specification<ClienteEntity> spec = createClienteSpecification(
                nombre, apellidoPaterno, apellidoMaterno, curp, rfc, correo, activo, fechaInicio, fechaFin
        );

        Page<ClienteEntity> pageResult = clienteRepository.findAll(spec, pageable);
        List<ClienteResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(content, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
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

        Specification<ClienteEntity> spec = createClienteSpecification(
                nombre, apellidoPaterno, apellidoMaterno, curp, rfc, correo, activo, fechaInicio, fechaFin
        );

        return clienteRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Specification<ClienteEntity> createClienteSpecification(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            String curp,
            String rfc,
            String correo,
            Boolean activo,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (StringUtils.isNotBlank(nombre)) p.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.trim().toLowerCase() + "%"));
            if (StringUtils.isNotBlank(apellidoPaterno)) p.add(cb.like(cb.lower(root.get("apellidoPaterno")), "%" + apellidoPaterno.trim().toLowerCase() + "%"));
            if (StringUtils.isNotBlank(apellidoMaterno)) p.add(cb.like(cb.lower(root.get("apellidoMaterno")), "%" + apellidoMaterno.trim().toLowerCase() + "%"));
            if (StringUtils.isNotBlank(curp)) p.add(cb.equal(cb.upper(root.get("curp")), curp.trim().toUpperCase()));
            if (StringUtils.isNotBlank(rfc)) p.add(cb.equal(cb.upper(root.get("rfc")), rfc.trim().toUpperCase()));
            if (StringUtils.isNotBlank(correo)) p.add(cb.like(cb.lower(root.get("correo")), "%" + correo.trim().toLowerCase() + "%"));
            if (activo != null) p.add(cb.equal(root.get("activo"), activo));
            if (fechaInicio != null) p.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaInicio.atStartOfDay()));
            if (fechaFin != null) p.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaFin.atTime(23, 59, 59)));
            return cb.and(p.toArray(new Predicate[0]));
        };
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

        Optional.ofNullable(request.getDomicilio())
                .filter(d -> cliente.getDomicilio() != null)
                .ifPresent(d -> updateDomicilio(cliente.getDomicilio(), d));

        return mapToResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse actualizarParcial(Integer id, ClientePatchRequest request) {
        log.info("Actualización parcial de cliente ID: {}", id);
        ClienteEntity c = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + id));

        Optional.ofNullable(request.getNombre()).map(String::trim).ifPresent(c::setNombre);
        Optional.ofNullable(request.getSegundoNombre()).map(StringUtils::trimToNull).ifPresent(c::setSegundoNombre);
        Optional.ofNullable(request.getApellidoPaterno()).map(String::trim).ifPresent(c::setApellidoPaterno);
        Optional.ofNullable(request.getApellidoMaterno()).map(String::trim).ifPresent(c::setApellidoMaterno);
        Optional.ofNullable(request.getFechaNacimiento()).ifPresent(c::setFechaNacimiento);
        Optional.ofNullable(request.getSexo()).map(s -> s.trim().toUpperCase()).ifPresent(c::setSexo);
        Optional.ofNullable(request.getNacionalidad()).map(String::trim).ifPresent(c::setNacionalidad);
        Optional.ofNullable(request.getEstadoCivil()).map(s -> s.trim().toUpperCase()).ifPresent(c::setEstadoCivil);
        Optional.ofNullable(request.getTelefonoMovil()).map(String::trim).ifPresent(c::setTelefonoMovil);
        Optional.ofNullable(request.getTelefonoAlternativo()).map(StringUtils::trimToNull).ifPresent(c::setTelefonoAlternativo);
        Optional.ofNullable(request.getOcupacion()).map(String::trim).ifPresent(c::setOcupacion);
        Optional.ofNullable(request.getEmpresa()).map(String::trim).ifPresent(c::setEmpresa);
        Optional.ofNullable(request.getIngresoMensual()).ifPresent(c::setIngresoMensual);

        Optional.ofNullable(request.getDomicilio())
                .filter(d -> c.getDomicilio() != null)
                .ifPresent(d -> updateDomicilio(c.getDomicilio(), d));

        return mapToResponse(clienteRepository.save(c));
    }

    private void updateDomicilio(DomicilioEntity dom, DomicilioDTO d) {
        Optional.ofNullable(d.getCalle()).map(String::trim).ifPresent(dom::setCalle);
        Optional.ofNullable(d.getNumeroExterior()).map(String::trim).ifPresent(dom::setNumeroExterior);
        Optional.ofNullable(d.getNumeroInterior()).map(StringUtils::trimToNull).ifPresent(dom::setNumeroInterior);
        Optional.ofNullable(d.getColonia()).map(String::trim).ifPresent(dom::setColonia);
        Optional.ofNullable(d.getMunicipio()).map(String::trim).ifPresent(dom::setMunicipio);
        Optional.ofNullable(d.getEstado()).map(String::trim).ifPresent(dom::setEstado);
        Optional.ofNullable(d.getCodigoPostal()).map(String::trim).ifPresent(dom::setCodigoPostal);
        Optional.ofNullable(d.getPais()).map(String::trim).ifPresent(dom::setPais);
        domicilioRepository.save(dom);
    }

    @Override
    @Transactional
    public void bajaLogica(Integer id) {
        log.info("Ejecutando baja lógica integral para cliente ID: {}", id);
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + id));

        cliente.setActivo(false);
        Optional.ofNullable(cliente.getCuentas()).ifPresent(cuentas -> cuentas.forEach(c -> {
            c.setEstatus("INACTIVA");
            cuentaRepository.save(c);
        }));
        Optional.ofNullable(cliente.getUsuario()).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.save(u);
        });

        clienteRepository.save(cliente);
        log.info("Baja lógica completada exitosamente para cliente ID: {}", id);
    }

    private CuentaEntity generarCuentaBancaria(ClienteEntity cliente, BigDecimal saldoInicial) {
        String numeroCuenta;
        do {
            numeroCuenta = String.format("%010d", secureRandom.nextInt(1_000_000_000));
        } while (cuentaRepository.existsByNumeroCuenta(numeroCuenta));

        return CuentaEntity.builder()
                .cliente(cliente)
                .numeroCuenta(numeroCuenta)
                .clabe("012180" + numeroCuenta + "01")
                .saldo(saldoInicial)
                .estatus("ACTIVA")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    private ClienteResponse mapToResponse(ClienteEntity cliente) {
        String nombreCompleto = Stream.of(cliente.getNombre(), cliente.getSegundoNombre(), cliente.getApellidoPaterno(), cliente.getApellidoMaterno())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "));

        DomicilioDTO domDTO = Optional.ofNullable(cliente.getDomicilio())
                .map(d -> DomicilioDTO.builder()
                        .calle(d.getCalle()).numeroExterior(d.getNumeroExterior()).numeroInterior(d.getNumeroInterior())
                        .colonia(d.getColonia()).municipio(d.getMunicipio()).estado(d.getEstado())
                        .codigoPostal(d.getCodigoPostal()).pais(d.getPais()).build())
                .orElse(null);

        List<CuentaResponse> cuentasResponse = Optional.ofNullable(cliente.getCuentas())
                .map(list -> list.stream().map(c -> CuentaResponse.builder()
                        .id(c.getId()).clienteId(cliente.getId()).numeroCuenta(c.getNumeroCuenta()).clabe(c.getClabe())
                        .saldo(c.getSaldo()).estatus(c.getEstatus()).fechaCreacion(c.getFechaCreacion())
                        .fechaActualizacion(c.getFechaActualizacion()).build()).toList())
                .orElseGet(List::of);

        UsuarioResponse usuarioResponse = Optional.ofNullable(cliente.getUsuario())
                .map(u -> UsuarioResponse.builder()
                        .id(u.getId()).clienteId(cliente.getId()).correo(u.getCorreo()).activo(u.getActivo())
                        .fechaCreacion(u.getFechaCreacion()).fechaActualizacion(u.getFechaActualizacion()).build())
                .orElse(null);

        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .segundoNombre(cliente.getSegundoNombre())
                .apellidoPaterno(cliente.getApellidoPaterno())
                .apellidoMaterno(cliente.getApellidoMaterno())
                .nombreCompleto(nombreCompleto)
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
