package com.riva.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riva.dto.CreatePedidoRequest;
import com.riva.dto.DireccionEnvioRequest;
import com.riva.dto.PedidoResponse;
import com.riva.dto.ProcesarPagoRequest;
import com.riva.dto.ProcesarPagoResponse;
import com.riva.model.pedido.Pedido;
import com.riva.service.PedidoService;

@RestController
@RequestMapping("/api/orders")
public class PedidoController {

    private static final String DEFAULT_CLIENTE_ID = "cliente-demo";

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public PedidoResponse crear(
            @RequestHeader(name = "X-Cliente-Id", defaultValue = DEFAULT_CLIENTE_ID) String clienteId,
            @RequestBody(required = false) CreatePedidoRequest request
    ) {
        Pedido pedido = pedidoService.crearDesdeCarrito(
                clienteId,
                request == null ? null : request.direccionEnvioModel()
        );
        return PedidoResponse.from(pedido);
    }

    @GetMapping
    public List<PedidoResponse> listar(
            @RequestHeader(name = "X-Cliente-Id", defaultValue = DEFAULT_CLIENTE_ID) String clienteId
    ) {
        return pedidoService.listarDelCliente(clienteId).stream()
                .map(PedidoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PedidoResponse detalle(
            @RequestHeader(name = "X-Cliente-Id", defaultValue = DEFAULT_CLIENTE_ID) String clienteId,
            @PathVariable String id
    ) {
        return PedidoResponse.from(pedidoService.obtenerDetalle(clienteId, id));
    }

    @PostMapping("/{id}/advance")
    public PedidoResponse avanzar(@PathVariable String id) {
        // Restriccion pendiente: cuando exista CU-03 + JWT, limitar a rol Administrador.
        return PedidoResponse.from(pedidoService.avanzarEstado(id));
    }

    @PostMapping("/{id}/payment")
    public ProcesarPagoResponse procesarPago(
            @RequestHeader(name = "X-Cliente-Id", defaultValue = DEFAULT_CLIENTE_ID) String clienteId,
            @PathVariable String id,
            @RequestBody ProcesarPagoRequest request
    ) {
        return pedidoService.procesarPago(clienteId, id, request);
    }

    @PatchMapping("/{id}/shipping-address")
    public PedidoResponse actualizarDireccion(
            @RequestHeader(name = "X-Cliente-Id", defaultValue = DEFAULT_CLIENTE_ID) String clienteId,
            @PathVariable String id,
            @RequestBody DireccionEnvioRequest request
    ) {
        return PedidoResponse.from(pedidoService.actualizarDireccionEnvio(clienteId, id, request.toModel()));
    }
}
