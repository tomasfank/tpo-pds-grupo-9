package com.riva.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riva.dto.AgregarItemCarritoRequest;
import com.riva.dto.CarritoResponse;
import com.riva.dto.ModificarCantidadCarritoRequest;
import com.riva.model.cart.Carrito;
import com.riva.security.UsuarioPrincipal;
import com.riva.service.CarritoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public CarritoResponse get(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return responder(carritoService.obtenerCarrito(principal.userId()));
    }

    @PostMapping("/items")
    public CarritoResponse agregarItem(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody AgregarItemCarritoRequest request) {
        return responder(carritoService.agregarItem(
                principal.userId(), request.productId(), request.variantId(), request.cantidad()));
    }

    @PatchMapping("/items/{itemId}")
    public CarritoResponse modificarCantidad(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable String itemId,
            @Valid @RequestBody ModificarCantidadCarritoRequest request) {
        return responder(
                carritoService.modificarCantidad(principal.userId(), itemId, request.cantidad()));
    }

    @DeleteMapping("/items/{itemId}")
    public CarritoResponse eliminarItem(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PathVariable String itemId) {
        return responder(carritoService.eliminarItem(principal.userId(), itemId));
    }

    @DeleteMapping("/items")
    public CarritoResponse vaciar(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return responder(carritoService.vaciar(principal.userId()));
    }

    // CU-12 — marca los items cuyo producto fue desactivado para avisar al cliente.
    private CarritoResponse responder(Carrito carrito) {
        return CarritoResponse.from(carrito, carritoService.productosInactivos(carrito));
    }
}
