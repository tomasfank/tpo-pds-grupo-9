package com.riva.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riva.dto.CheckoutRequest;
import com.riva.dto.CheckoutResponse;
import com.riva.dto.CreatePedidoRequest;
import com.riva.dto.DireccionEnvioRequest;
import com.riva.dto.PedidoResponse;
import com.riva.dto.ProcesarPagoRequest;
import com.riva.dto.ProcesarPagoResponse;
import com.riva.model.pedido.Pedido;
import com.riva.security.UsuarioPrincipal;
import com.riva.service.PedidoService;
import com.riva.service.TiendaFacade;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class PedidoController {

    private final PedidoService pedidoService;
    private final TiendaFacade tiendaFacade;

    public PedidoController(PedidoService pedidoService, TiendaFacade tiendaFacade) {
        this.pedidoService = pedidoService;
        this.tiendaFacade = tiendaFacade;
    }

    @PostMapping
    public PedidoResponse crear(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestBody(required = false) CreatePedidoRequest request) {
        Pedido pedido = pedidoService.crearDesdeCarrito(
                principal.userId(),
                request == null ? null : request.direccionEnvioModel());
        return PedidoResponse.from(pedido);
    }

    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return pedidoService.listarDelCliente(principal.userId()).stream()
                .map(PedidoResponse::from)
                .toList();
    }

    @GetMapping("/admin")
    public List<PedidoResponse> listarTodos() {
        // CU-23 — listado de todos los pedidos. Restringido a ROLE_ADMINISTRADOR en SecurityConfig.
        return pedidoService.listarTodos().stream()
                .map(PedidoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PedidoResponse detalle(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable String id) {
        return PedidoResponse.from(pedidoService.obtenerDetalle(principal.userId(), id));
    }

    @PostMapping("/{id}/advance")
    public PedidoResponse avanzar(@PathVariable String id) {
        // Restringido a ROLE_ADMINISTRADOR en SecurityConfig (CU-23).
        return PedidoResponse.from(pedidoService.avanzarEstado(id));
    }

    @PostMapping("/checkout")
    public CheckoutResponse checkout(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody CheckoutRequest request) {
        // Patron Facade: confirma la compra completa (crear pedido + pagar + notificar) en un paso.
        return tiendaFacade.confirmarCompra(
                principal.userId(),
                request.pago(),
                request.direccionEnvio() == null ? null : request.direccionEnvio().toModel());
    }

    @PostMapping("/{id}/payment")
    public ProcesarPagoResponse procesarPago(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable String id,
            @RequestBody ProcesarPagoRequest request) {
        return pedidoService.procesarPago(principal.userId(), id, request);
    }

    @PatchMapping("/{id}/shipping-address")
    public PedidoResponse actualizarDireccion(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable String id,
            @RequestBody DireccionEnvioRequest request) {
        return PedidoResponse.from(
                pedidoService.actualizarDireccionEnvio(principal.userId(), id, request.toModel()));
    }
}
