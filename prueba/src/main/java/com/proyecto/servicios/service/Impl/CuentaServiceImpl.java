package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.ClienteEntity;
import com.proyecto.servicios.entity.sf.CuentaEntity;
import com.proyecto.servicios.exception.ClienteBusinessException;
import com.proyecto.servicios.exception.ClienteNotFoundException;
import com.proyecto.servicios.exception.CuentaNotFoundException;
import com.proyecto.servicios.model.cuenta.CuentaActualizaRequest;
import com.proyecto.servicios.model.cuenta.CuentaCreacionRequest;
import com.proyecto.servicios.model.cuenta.CuentaResponse;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.service.CuentaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public CuentaServiceImpl(CuentaRepository cuentaRepository, ClienteRepository clienteRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public CuentaResponse crearCuenta(CuentaCreacionRequest request) {
        log.info("Creando cuenta bancaria adicional para cliente ID: {}", request.getClienteId());

        ClienteEntity cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró ningún cliente con el ID: " + request.getClienteId()));

        if (Boolean.FALSE.equals(cliente.getActivo())) {
            throw new ClienteBusinessException("Solo los clientes activos pueden aperturar cuentas bancarias");
        }

        BigDecimal saldoInicial = request.getSaldoInicial() != null ? request.getSaldoInicial() : BigDecimal.ZERO;
        if (saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new ClienteBusinessException("El saldo inicial no puede ser negativo");
        }

        String numeroCuenta;
        do {
            numeroCuenta = String.format("%010d", secureRandom.nextInt(1_000_000_000));
        } while (cuentaRepository.existsByNumeroCuenta(numeroCuenta));

        String clabe = "012180" + numeroCuenta + "01";

        CuentaEntity nuevaCuenta = CuentaEntity.builder()
                .cliente(cliente)
                .numeroCuenta(numeroCuenta)
                .clabe(clabe)
                .saldo(saldoInicial)
                .estatus("ACTIVA")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        CuentaEntity guardada = cuentaRepository.save(nuevaCuenta);
        log.info("Cuenta bancaria creada exitosamente. Número: {}", guardada.getNumeroCuenta());

        return mapToResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponse consultarPorNumeroCuenta(String numeroCuenta) {
        log.info("Consultando cuenta bancaria por número: {}", numeroCuenta);
        CuentaEntity cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta.trim())
                .orElseThrow(() -> new CuentaNotFoundException("No se encontró la cuenta bancaria con número: " + numeroCuenta));
        return mapToResponse(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarPorClienteId(Integer clienteId) {
        log.info("Consultando cuentas bancarias para cliente ID: {}", clienteId);
        if (!clienteRepository.existsById(clienteId)) {
            throw new ClienteNotFoundException("No se encontró el cliente con ID: " + clienteId);
        }
        return cuentaRepository.findByClienteId(clienteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> consultarPorEstatus(String estatus) {
        log.info("Consultando cuentas bancarias por estatus: {}", estatus);
        return cuentaRepository.findByEstatus(estatus.trim().toUpperCase()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CuentaResponse actualizarParcial(String numeroCuenta, CuentaActualizaRequest request) {
        log.info("Actualizando parcialmente cuenta bancaria: {}", numeroCuenta);
        CuentaEntity cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta.trim())
                .orElseThrow(() -> new CuentaNotFoundException("No se encontró la cuenta bancaria con número: " + numeroCuenta));

        if (StringUtils.isNotBlank(request.getEstatus())) {
            String nuevoEstatus = request.getEstatus().trim().toUpperCase();
            if ("ACTIVA".equalsIgnoreCase(nuevoEstatus) && Boolean.FALSE.equals(cuenta.getCliente().getActivo())) {
                throw new ClienteBusinessException("Solo los clientes activos podrán tener cuentas activas");
            }
            cuenta.setEstatus(nuevoEstatus);
        }

        if (request.getSaldo() != null) {
            if (request.getSaldo().compareTo(BigDecimal.ZERO) < 0) {
                throw new ClienteBusinessException("El saldo no puede ser negativo");
            }
            cuenta.setSaldo(request.getSaldo());
        }

        CuentaEntity actualizada = cuentaRepository.save(cuenta);
        log.info("Cuenta bancaria actualizada exitosamente: {}", actualizada.getNumeroCuenta());
        return mapToResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal consultarSaldo(String numeroCuenta) {
        log.info("Consultando saldo para cuenta bancaria: {}", numeroCuenta);
        CuentaEntity cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta.trim())
                .orElseThrow(() -> new CuentaNotFoundException("No se encontró la cuenta bancaria con número: " + numeroCuenta));
        return cuenta.getSaldo();
    }

    private CuentaResponse mapToResponse(CuentaEntity cuenta) {
        return CuentaResponse.builder()
                .id(cuenta.getId())
                .clienteId(cuenta.getCliente() != null ? cuenta.getCliente().getId() : null)
                .numeroCuenta(cuenta.getNumeroCuenta())
                .clabe(cuenta.getClabe())
                .saldo(cuenta.getSaldo())
                .estatus(cuenta.getEstatus())
                .fechaCreacion(cuenta.getFechaCreacion())
                .fechaActualizacion(cuenta.getFechaActualizacion())
                .build();
    }
}
