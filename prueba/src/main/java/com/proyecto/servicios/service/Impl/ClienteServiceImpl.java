package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.ClienteEntity;
import com.proyecto.servicios.entity.sf.CuentaBancariaEntity;
import com.proyecto.servicios.exception.ClienteBusinessException;
import com.proyecto.servicios.exception.ClienteNotFoundException;
import com.proyecto.servicios.model.cliente.*;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaBancariaRepository;
import com.proyecto.servicios.service.ClienteService;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public ClienteServiceImpl(ClienteRepository clienteRepository, CuentaBancariaRepository cuentaBancariaRepository) {
        this.clienteRepository = clienteRepository;
        this.cuentaBancariaRepository = cuentaBancariaRepository;
    }

    @Override
    @Transactional
    public ClienteResponse registrarCliente(ClienteRegistroRequest request) {
        log.info("Iniciando proceso de registro para cliente con CURP: {}", request.getCurp());

        String curp = request.getCurp().trim().toUpperCase();
        String rfc = request.getRfc().trim().toUpperCase();

        if (clienteRepository.existsByCurp(curp)) {
            throw new ClienteBusinessException("Ya existe un cliente registrado con la CURP: " + curp);
        }

        if (clienteRepository.existsByRfc(rfc)) {
            throw new ClienteBusinessException("Ya existe un cliente registrado con el RFC: " + rfc);
        }

        int edad = Period.between(request.getFechaNacimiento(), LocalDate.now()).getYears();
        if (edad < 18) {
            throw new ClienteBusinessException("El cliente debe ser mayor de edad para registrarse (edad actual calculada: " + edad + " años)");
        }

        ClienteEntity cliente = ClienteEntity.builder()
                .nombre(request.getNombre().trim())
                .segundoNombre(StringUtils.trimToNull(request.getSegundoNombre()))
                .apellidoPaterno(request.getApellidoPaterno().trim())
                .apellidoMaterno(StringUtils.trimToNull(request.getApellidoMaterno()))
                .fechaNacimiento(request.getFechaNacimiento())
                .curp(curp)
                .rfc(rfc)
                .telefono(StringUtils.trimToNull(request.getTelefono()))
                .email(StringUtils.trimToNull(request.getEmail()))
                .calle(StringUtils.trimToNull(request.getCalle()))
                .numeroExterior(StringUtils.trimToNull(request.getNumeroExterior()))
                .colonia(StringUtils.trimToNull(request.getColonia()))
                .codigoPostal(StringUtils.trimToNull(request.getCodigoPostal()))
                .ciudad(StringUtils.trimToNull(request.getCiudad()))
                .estado(StringUtils.trimToNull(request.getEstado()))
                .puestoLaboral(StringUtils.trimToNull(request.getPuestoLaboral()))
                .ingresoMensual(request.getIngresoMensual() != null ? request.getIngresoMensual() : BigDecimal.ZERO)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        ClienteEntity clienteGuardado = clienteRepository.save(cliente);

        BigDecimal saldoInicial = (request.getSaldoInicial() != null && request.getSaldoInicial().compareTo(BigDecimal.ZERO) >= 0)
                ? request.getSaldoInicial()
                : new BigDecimal("1000.00");

        CuentaBancariaEntity cuenta = generarCuentaBancaria(clienteGuardado, saldoInicial);
        CuentaBancariaEntity cuentaGuardada = cuentaBancariaRepository.save(cuenta);
        clienteGuardado.setCuentaBancaria(cuentaGuardada);

        log.info("Cliente registrado exitosamente con ID: {}, cuenta bancaria: {}", clienteGuardado.getId(), cuentaGuardada.getNumeroCuenta());
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
    public List<ClienteResponse> consultarClientes(String nombre, String curp, String rfc, Boolean activo) {
        log.info("Consultando clientes con filtros - nombre: {}, curp: {}, rfc: {}, activo: {}", nombre, curp, rfc, activo);

        Specification<ClienteEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(nombre)) {
                predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.trim().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(curp)) {
                predicates.add(cb.equal(cb.upper(root.get("curp")), curp.trim().toUpperCase()));
            }
            if (StringUtils.isNotBlank(rfc)) {
                predicates.add(cb.equal(cb.upper(root.get("rfc")), rfc.trim().toUpperCase()));
            }
            if (activo != null) {
                predicates.add(cb.equal(root.get("activo"), activo));
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
        log.info("Actualizando totalmente cliente con ID: {}", id);
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + id));

        cliente.setNombre(request.getNombre().trim());
        cliente.setSegundoNombre(StringUtils.trimToNull(request.getSegundoNombre()));
        cliente.setApellidoPaterno(request.getApellidoPaterno().trim());
        cliente.setApellidoMaterno(StringUtils.trimToNull(request.getApellidoMaterno()));
        cliente.setTelefono(StringUtils.trimToNull(request.getTelefono()));
        cliente.setEmail(StringUtils.trimToNull(request.getEmail()));
        cliente.setCalle(StringUtils.trimToNull(request.getCalle()));
        cliente.setNumeroExterior(StringUtils.trimToNull(request.getNumeroExterior()));
        cliente.setColonia(StringUtils.trimToNull(request.getColonia()));
        cliente.setCodigoPostal(StringUtils.trimToNull(request.getCodigoPostal()));
        cliente.setCiudad(StringUtils.trimToNull(request.getCiudad()));
        cliente.setEstado(StringUtils.trimToNull(request.getEstado()));
        cliente.setPuestoLaboral(StringUtils.trimToNull(request.getPuestoLaboral()));
        if (request.getIngresoMensual() != null) {
            cliente.setIngresoMensual(request.getIngresoMensual());
        }

        ClienteEntity actualizado = clienteRepository.save(cliente);
        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public ClienteResponse actualizarParcial(Integer id, ClientePatchRequest request) {
        log.info("Actualización parcial para cliente con ID: {}", id);
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + id));

        if (request.getNombre() != null) cliente.setNombre(request.getNombre().trim());
        if (request.getSegundoNombre() != null) cliente.setSegundoNombre(StringUtils.trimToNull(request.getSegundoNombre()));
        if (request.getApellidoPaterno() != null) cliente.setApellidoPaterno(request.getApellidoPaterno().trim());
        if (request.getApellidoMaterno() != null) cliente.setApellidoMaterno(StringUtils.trimToNull(request.getApellidoMaterno()));
        if (request.getTelefono() != null) cliente.setTelefono(StringUtils.trimToNull(request.getTelefono()));
        if (request.getEmail() != null) cliente.setEmail(StringUtils.trimToNull(request.getEmail()));
        if (request.getCalle() != null) cliente.setCalle(StringUtils.trimToNull(request.getCalle()));
        if (request.getNumeroExterior() != null) cliente.setNumeroExterior(StringUtils.trimToNull(request.getNumeroExterior()));
        if (request.getColonia() != null) cliente.setColonia(StringUtils.trimToNull(request.getColonia()));
        if (request.getCodigoPostal() != null) cliente.setCodigoPostal(StringUtils.trimToNull(request.getCodigoPostal()));
        if (request.getCiudad() != null) cliente.setCiudad(StringUtils.trimToNull(request.getCiudad()));
        if (request.getEstado() != null) cliente.setEstado(StringUtils.trimToNull(request.getEstado()));
        if (request.getPuestoLaboral() != null) cliente.setPuestoLaboral(StringUtils.trimToNull(request.getPuestoLaboral()));
        if (request.getIngresoMensual() != null) cliente.setIngresoMensual(request.getIngresoMensual());

        ClienteEntity actualizado = clienteRepository.save(cliente);
        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public void bajaLogica(Integer id) {
        log.info("Ejecutando baja lógica para cliente con ID: {}", id);
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + id));

        cliente.setActivo(false);
        if (cliente.getCuentaBancaria() != null) {
            cliente.getCuentaBancaria().setActivo(false);
        }
        clienteRepository.save(cliente);
        log.info("Baja lógica completada para cliente ID: {}", id);
    }

    private CuentaBancariaEntity generarCuentaBancaria(ClienteEntity cliente, BigDecimal saldoInicial) {
        String numeroCuenta;
        do {
            numeroCuenta = String.format("%010d", secureRandom.nextInt(1_000_000_000));
        } while (cuentaBancariaRepository.existsByNumeroCuenta(numeroCuenta));

        String clabe = "012180" + numeroCuenta + "01";

        return CuentaBancariaEntity.builder()
                .cliente(cliente)
                .numeroCuenta(numeroCuenta)
                .clabe(clabe)
                .saldo(saldoInicial)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    private ClienteResponse mapToResponse(ClienteEntity cliente) {
        StringBuilder nombreCompleto = new StringBuilder(cliente.getNombre());
        if (StringUtils.isNotBlank(cliente.getSegundoNombre())) {
            nombreCompleto.append(" ").append(cliente.getSegundoNombre());
        }
        nombreCompleto.append(" ").append(cliente.getApellidoPaterno());
        if (StringUtils.isNotBlank(cliente.getApellidoMaterno())) {
            nombreCompleto.append(" ").append(cliente.getApellidoMaterno());
        }

        CuentaBancariaResponse cuentaResponse = null;
        if (cliente.getCuentaBancaria() != null) {
            CuentaBancariaEntity c = cliente.getCuentaBancaria();
            cuentaResponse = CuentaBancariaResponse.builder()
                    .id(c.getId())
                    .numeroCuenta(c.getNumeroCuenta())
                    .clabe(c.getClabe())
                    .saldo(c.getSaldo())
                    .activo(c.getActivo())
                    .fechaCreacion(c.getFechaCreacion())
                    .build();
        }

        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .segundoNombre(cliente.getSegundoNombre())
                .apellidoPaterno(cliente.getApellidoPaterno())
                .apellidoMaterno(cliente.getApellidoMaterno())
                .nombreCompleto(nombreCompleto.toString())
                .fechaNacimiento(cliente.getFechaNacimiento())
                .curp(cliente.getCurp())
                .rfc(cliente.getRfc())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .calle(cliente.getCalle())
                .numeroExterior(cliente.getNumeroExterior())
                .colonia(cliente.getColonia())
                .codigoPostal(cliente.getCodigoPostal())
                .ciudad(cliente.getCiudad())
                .estado(cliente.getEstado())
                .puestoLaboral(cliente.getPuestoLaboral())
                .ingresoMensual(cliente.getIngresoMensual())
                .activo(cliente.getActivo())
                .fechaCreacion(cliente.getFechaCreacion())
                .fechaActualizacion(cliente.getFechaActualizacion())
                .cuentaBancaria(cuentaResponse)
                .build();
    }
}
